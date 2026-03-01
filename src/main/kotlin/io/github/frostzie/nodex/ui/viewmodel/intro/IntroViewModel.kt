package io.github.frostzie.nodex.ui.viewmodel.intro

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.services.ui.NavigationService

class IntroViewModel(
    private val navigationService: NavigationService
) {

    fun tutorialStart() {
        // TODO: Tutorial
    }

    fun tutorialSkip() {
        navigationService.navigateTo(AppScreen.IDE) //TODO: Replace with Project Manegr
    }
}