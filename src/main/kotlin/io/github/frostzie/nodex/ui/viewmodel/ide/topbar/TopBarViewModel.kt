package io.github.frostzie.nodex.ui.viewmodel.ide.topbar

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.services.ui.NavigationService
import io.github.frostzie.nodex.services.workspace.WorkspaceService
import java.nio.file.Path

/**
 * ViewModel for the TopBarView.
 */
class TopBarViewModel(
    private val workspaceService: WorkspaceService,
    private val navigationService: NavigationService
) {

    fun openFolder(path: Path) {
        workspaceService.openSingleProject(path)
    }

    fun openIntro() {
        navigationService.navigateTo(AppScreen.INTRO)
    }

    fun openProjectManager() {
        navigationService.navigateTo(AppScreen.PROJECT_MANAGER)
    }

    fun openIde() {
        navigationService.navigateTo(AppScreen.IDE)
    }

    fun openSettings() {
        navigationService.showOverlay(OverlayScreen.SETTINGS)
    }
}
