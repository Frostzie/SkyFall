package io.github.frostzie.nodex.ui.viewmodel.ide.topbar

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.services.ui.NavigationService

/**
 * ViewModel for the TopBarView.
 */
class TopBarViewModel(
    private val navigationService: NavigationService
) {

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
