package io.github.frostzie.nodex.ui.viewmodel.intro

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.api.navigation.Navigation

class IntroViewModel(
    private val navigationService: Navigation
) {

    fun tutorialStart() {
        // TODO: Tutorial
    }

    fun tutorialSkip() {
        navigationService.navigateTo(AppScreen.PROJECT_MANAGER)
    }
}