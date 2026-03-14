package io.github.frostzie.nodex.services.ui

import ch.micheljung.fxwindow.FxStage
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.domain.uicontract.WindowPolicy
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.ui.ViewFactory
import io.github.frostzie.nodex.ui.ScreenRegistry
import io.github.frostzie.nodex.ui.util.WindowGeometryTracker
import io.github.frostzie.nodex.ui.util.applyBasePolicy
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Modality
import javafx.stage.Stage

/**
 * Service responsible for managing modal app windows (Overlays).
 *
 * It observes the [NavigationService] and creates/shows/hides separate
 * JavaFX Stages based on the active [OverlayScreen].
 */
open class OverlayStageService(
    private val layoutService: LayoutService,
    private val navigationService: NavigationService,
    private val focusService: FocusService,
    private val viewFactory: ViewFactory
) {
    private val logger = LoggerProvider.getLogger("OverlayStageService")
    private val activeStages = mutableMapOf<OverlayScreen, Stage>()
    private var primaryWindowStage: Stage? = null

    /**
     * Sets the primary window stage to be used as the owner for modals.
     */
    fun setPrimaryStage(stage: Stage) {
        this.primaryWindowStage = stage
    }

    /**
     * Initializes the service by observing overlay changes.
     */
    fun initialize() {
        setupNavigationListeners()
    }

    private fun setupNavigationListeners() {
        navigationService.activeOverlay.addListener { _, _, newOverlay ->
            if (newOverlay != null) {
                showOverlay(newOverlay)
            } else {
                closeAllOverlays()
            }
        }
    }

    private fun showOverlay(overlay: OverlayScreen) {
        if (activeStages.containsKey(overlay)) {
            activeStages[overlay]?.toFront()
            return
        }

        val stage = Stage()
        val rootNode = viewFactory.createOverlayContent(overlay)
        val scene = Scene(rootNode)

        configureFxStage(stage, rootNode, scene)
        val tracker = setupGeometryTracker(stage, overlay)
        applyPolicy(stage, rootNode, overlay, tracker)

        stage.setOnCloseRequest { event ->
            event.consume()
            navigationService.closeOverlay()
        }

        activeStages[overlay] = stage
        stage.show()
        focusService.trackStage(stage)
    }

    private fun configureFxStage(stage: Stage, rootNode: Region, scene: Scene) {
        try {
            FxStage.configure(stage)
                .withSceneFactory { parent -> scene.apply { root = parent } }
                .withContent(rootNode)
                .allowMinimize(false)
                .apply()
        } catch (e: Exception) {
            logger.error("Failed to configure FxStage for overlay", e)
            stage.scene = scene
        }
    }

    private fun setupGeometryTracker(stage: Stage, overlay: OverlayScreen): WindowGeometryTracker {
        return WindowGeometryTracker(stage) { newState ->
            val profile = ScreenRegistry.getProfile(overlay)
            if (profile.isPersistent) {
                layoutService.updateOverlayWindowState(overlay, newState)
            }
        }
    }

    private fun applyPolicy(
        stage: Stage,
        rootNode: Region,
        overlay: OverlayScreen,
        tracker: WindowGeometryTracker
    ) {
        val profile = ScreenRegistry.getProfile(overlay)
        val policy = WindowPolicy(
            title = profile.title,
            minWidth = profile.minWidth,
            minHeight = profile.minHeight,
            prefWidth = profile.prefWidth,
            prefHeight = profile.prefHeight,
            isResizable = profile.isResizable,
            isPersistent = profile.isPersistent,
            isModal = profile.isModal,
            alwaysOnTop = profile.alwaysOnTop
        )

        val state = layoutService.getOverlayWindowState(overlay)

        // Overlay specific settings
        primaryWindowStage?.let { stage.initOwner(it) }
        if (policy.isModal) {
            stage.initModality(Modality.APPLICATION_MODAL)
        }
        stage.isAlwaysOnTop = policy.alwaysOnTop

        // Shared logic via extension
        stage.applyBasePolicy(rootNode, policy, state, tracker)
    }

    private fun closeAllOverlays() {
        activeStages.values.forEach { it.close() }
        activeStages.clear()
    }
}
