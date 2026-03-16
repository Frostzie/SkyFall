package io.github.frostzie.nodex.services.core

import io.github.frostzie.nodex.domain.config.*
import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.services.config.project.LayoutConfigService
import io.github.frostzie.nodex.services.ui.ToolWindowService
import io.github.frostzie.nodex.ui.ScreenRegistry
import java.nio.file.Path

/**
 * Service responsible for tracking and providing access to the UI layout state.
 */
class LayoutService(val toolWindowService: ToolWindowService) {
    private val windowStates = mutableMapOf<String, WindowBounds>()

    /**
     * Loads layout state for a specific project.
     */
    fun loadForProject(projectRoot: Path, layoutConfigService: LayoutConfigService) {
        val config = layoutConfigService.load(projectRoot)
        updateFromConfig(config)
    }

    /**
     * Saves layout state for a specific project.
     */
    fun saveForProject(projectRoot: Path, layoutConfigService: LayoutConfigService) {
        val config = createConfigFromCurrentState()
        layoutConfigService.save(projectRoot, config)
    }

    private fun updateFromConfig(config: LayoutConfig) {
        windowStates.clear()
        windowStates.putAll(config.windows)

        toolWindowService.initializeFromConfig(config.workbench.toolWindows)
    }

    private fun createConfigFromCurrentState(): LayoutConfig {
        val persistentWindows = windowStates.filterKeys { screenName ->
            try {
                val appScreen = AppScreen.valueOf(screenName)
                ScreenRegistry.getProfile(appScreen).isPersistent
            } catch (_: Exception) {
                try {
                    val overlayScreen = OverlayScreen.valueOf(screenName)
                    ScreenRegistry.getProfile(overlayScreen).isPersistent
                } catch (_: Exception) {
                    false
                }
            }
        }.toMutableMap()

        return LayoutConfig(
            windows = persistentWindows,
            workbench = WorkbenchLayout(
                toolWindows = toolWindowService.createConfigs().toMutableMap()
            )
        )
    }

    fun getWindowState(screen: AppScreen): WindowBounds =
        windowStates.getOrPut(screen.name) { WindowBounds() }

    fun updateWindowState(screen: AppScreen, bounds: WindowBounds) {
        windowStates[screen.name] = bounds
    }

    fun getOverlayWindowState(overlay: OverlayScreen): WindowBounds =
        windowStates.getOrPut(overlay.name) { WindowBounds() }

    fun updateOverlayWindowState(overlay: OverlayScreen, bounds: WindowBounds) {
        windowStates[overlay.name] = bounds
    }
}
