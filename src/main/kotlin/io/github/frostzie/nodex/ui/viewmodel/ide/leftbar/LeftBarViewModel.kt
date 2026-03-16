package io.github.frostzie.nodex.ui.viewmodel.ide.leftbar

import io.github.frostzie.nodex.domain.uicontract.ToolWindow
import io.github.frostzie.nodex.services.core.LayoutService

class LeftBarViewModel(private val layoutService: LayoutService) {

    fun toggleFileTree() {
        val toolWindowService = layoutService.toolWindowService
        val filesState = toolWindowService.states.find { it.toolType == ToolWindow.FILES } ?: return
        toolWindowService.setVisible(ToolWindow.FILES, !filesState.visible)
    }
}