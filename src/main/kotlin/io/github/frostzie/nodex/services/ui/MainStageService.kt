package io.github.frostzie.nodex.services.ui

import ch.micheljung.fxwindow.FxStage
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.WindowPolicy
import io.github.frostzie.nodex.services.core.LayoutService
import io.github.frostzie.nodex.ui.util.WindowGeometryTracker
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.layout.Region
import javafx.stage.Stage

/**
 * Service responsible for managing the mod's primary JavaFX Stage.
 *
 * Handles the window lifecycle, geometry tracking, and integration with
 * FxStage lib for native window behavior.
 */
open class MainStageService(
    private val layoutService: LayoutService,
    private val navigationService: NavigationService,
    private val focusService: FocusService
) {
    private val logger = LoggerProvider.getLogger("MainStageService")
    private var stage: Stage? = null
    private var content: Region? = null
    private var fxStage: FxStage? = null
    private var tracker: WindowGeometryTracker? = null
    private val pendingNonCaptionNodes = mutableListOf<Node>()

    /**
     * Initialize the service with the primary stage and content.
     */
    fun initialize(primaryStage: Stage, content: Region, scene: Scene) {
        check(stage == null) { "MainStageService has already been initialized." }
        this.stage = primaryStage
        this.content = content

        configureFxStage(primaryStage, content, scene)

        // Setup Geometry Tracker
        tracker = WindowGeometryTracker(primaryStage) { newState ->
            val screen = navigationService.currentScreen.get()
            if (screen != AppScreen.INTRO) {
                layoutService.updateWindowState(screen, newState)
            }
        }

        focusService.trackStage(primaryStage)
        setupScreenListeners()

        primaryStage.setOnCloseRequest { event ->
            event.consume()
            hide() // Minimize instead of closing so FxStage doesn't break
        }
    }

    fun show() {
        val currentStage = stage ?: return
        val screen = navigationService.currentScreen.get()

        if (!currentStage.isShowing) {
            applyPolicy(currentStage, screen)
            currentStage.show()
        }

        if (currentStage.isIconified) {
            currentStage.isIconified = false
        }

        currentStage.toFront()
        currentStage.requestFocus()
    }

    private fun configureFxStage(primaryStage: Stage, content: Region, scene: Scene) {
        try {
            fxStage = FxStage.configure(primaryStage)
                .allowTopResize(true)
                .allowMinimize(true)
                .withSceneFactory { parent -> scene.apply { root = parent } }
                .withContent(content)
                .apply()
            flushPendingNonCaptionNodes()
            logger.debug("FxStage configured successfully for primary stage")
        } catch (e: Exception) {
            logger.error("Failed to configure FxStage", e)
            scene.root = content
            primaryStage.scene = scene
        }
    }

    private fun setupScreenListeners() {
        navigationService.currentScreen.addListener { _, _, newScreen ->
            val currentStage = stage ?: return@addListener
            if (currentStage.isShowing) {
                applyPolicy(currentStage, newScreen)
            }
        }
    }

    private fun applyPolicy(currentStage: Stage, screen: AppScreen) {
        val policy = WindowPolicy.forScreen(screen)
        val state = layoutService.getWindowState(screen)

        currentStage.title = policy.title

        // Applying min size to content instead of stage
        // because FxStage binds stage min size to content min size
        content?.let {
            it.minWidth = policy.minWidth
            it.minHeight = policy.minHeight
        }

        currentStage.isResizable = policy.isResizable

        tracker?.applyState(state)
    }

    fun registerNonCaptionNodes(nodes: Collection<Node>) {
        if (nodes.isEmpty()) return
        val configuredFxStage = fxStage
        if (configuredFxStage != null) {
            configuredFxStage.nonCaptionNodes.addAll(nodes)
        } else {
            pendingNonCaptionNodes.addAll(nodes)
        }
    }

    private fun flushPendingNonCaptionNodes() {
        val configuredFxStage = fxStage ?: return
        configuredFxStage.nonCaptionNodes.addAll(pendingNonCaptionNodes)
        pendingNonCaptionNodes.clear()
    }

    fun hide() {
        stage?.let { it.isIconified = true }
    }

    fun isShowing(): Boolean = stage?.isShowing ?: false
    fun isIconified(): Boolean = stage?.isIconified ?: false
}
