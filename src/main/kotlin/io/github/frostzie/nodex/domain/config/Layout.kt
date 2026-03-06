package io.github.frostzie.nodex.domain.config

import io.github.frostzie.nodex.domain.uicontract.AppScreen

data class WindowState(
    val x: Double,
    val y: Double,
    val width: Double,
    val height: Double,
    val isMaximized: Boolean
)

interface WindowLayout {
    var x: Double
    var y: Double
    var width: Double
    var height: Double
    var isMaximized: Boolean
}

data class LayoutConfig(
    var activeScreen: AppScreen? = null,
    var ide: IdeWindowLayout = IdeWindowLayout(),
    var projectManager: ProjectManagerWindowLayout = ProjectManagerWindowLayout(),
    var settings: SettingsWindowLayout = SettingsWindowLayout()
)

// Main Screen Layout

data class IdeWindowLayout(
    override var x: Double = -1.0,
    override var y: Double = -1.0,
    override var width: Double = 1200.0,
    override var height: Double = 800.0,
    override var isMaximized: Boolean = false,
    var workbench: WorkbenchLayout = WorkbenchLayout()
) : WindowLayout

data class WorkbenchLayout(
    var sidebarPosition: String = "LEFT",
    var sidebarVisible: Boolean = true,
    var sidebarSize: Double = 0.25
)

data class ProjectManagerWindowLayout(
    override var x: Double = -1.0,
    override var y: Double = -1.0,
    override var width: Double = 800.0,
    override var height: Double = 600.0,
    override var isMaximized: Boolean = false
) : WindowLayout

// Overlay Screen Layouts

data class SettingsWindowLayout(
    override var x: Double = -1.0,
    override var y: Double = -1.0,
    override var width: Double = 950.0,
    override var height: Double = 750.0,
    override var isMaximized: Boolean = false
) : WindowLayout