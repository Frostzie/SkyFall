package io.github.frostzie.nodex.services.core

import io.github.frostzie.nodex.api.config.LayoutPersistence
import io.github.frostzie.nodex.api.navigation.Layout
import io.github.frostzie.nodex.api.navigation.ToolWindowProvider
import io.github.frostzie.nodex.api.navigation.WindowProfile
import io.github.frostzie.nodex.domain.config.*
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import java.nio.file.Path

/**
 * Service responsible for tracking and providing access to the UI layout state.
 */
class LayoutService(
    override val toolWindowProvider: ToolWindowProvider,
    private val windowProfile: WindowProfile,
    private val layoutConfigPersistence: LayoutPersistence
) : Layout {
    private val windowStates = mutableMapOf<String, WindowBounds>()

    /**
     * Loads layout state for a specific project.
     */
    override fun loadForProject(projectRoot: Path) {
        val config = layoutConfigPersistence.load(projectRoot)
        updateFromConfig(config)
    }

    /**
     * Saves layout state for a specific project.
     */
    override fun saveForProject(projectRoot: Path) {
        val config = createConfigFromCurrentState()
        layoutConfigPersistence.save(projectRoot, config)
    }

    private fun updateFromConfig(config: LayoutConfig) {
        windowStates.clear()
        windowStates.putAll(config.windows)

        toolWindowProvider.initializeFromConfig(config.workbench.toolWindows)
    }

    private fun createConfigFromCurrentState(): LayoutConfig {
        val persistentWindows = windowStates.filterKeys { screenName ->
            try {
                val appScreen = AppScreen.valueOf(screenName)
                windowProfile.getScreenPolicy(appScreen).isPersistent
            } catch (_: Exception) {
                try {
                    val overlayScreen = OverlayScreen.valueOf(screenName)
                    windowProfile.getOverlayPolicy(overlayScreen).isPersistent
                } catch (_: Exception) {
                    false
                }
            }
        }.toMutableMap()

        return LayoutConfig(
            windows = persistentWindows,
            workbench = WorkbenchLayout(
                toolWindows = toolWindowProvider.createConfigs().toMutableMap()
            )
        )
    }

    override fun getWindowState(screen: AppScreen): WindowBounds =
        windowStates.getOrPut(screen.name) { WindowBounds() }

    override fun updateWindowState(screen: AppScreen, bounds: WindowBounds) {
        windowStates[screen.name] = bounds
    }

    override fun getOverlayWindowState(overlay: OverlayScreen): WindowBounds =
        windowStates.getOrPut(overlay.name) { WindowBounds() }

    override fun updateOverlayWindowState(overlay: OverlayScreen, bounds: WindowBounds) {
        windowStates[overlay.name] = bounds
    }
}
