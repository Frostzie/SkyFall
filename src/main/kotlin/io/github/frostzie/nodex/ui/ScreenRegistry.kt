package io.github.frostzie.nodex.ui

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.domain.uicontract.WindowPolicy

/**
 * Registry for screen-specific metadata and layout policies.
 */
object ScreenRegistry {

    private val appScreens = mapOf(
        AppScreen.INTRO to WindowPolicy(
            title = "Nodex - Welcome",
            prefWidth = 825.0,
            prefHeight = 750.0,
            isResizable = false,
            isPersistent = false
        ),
        AppScreen.PROJECT_MANAGER to WindowPolicy(
            title = "Nodex - Project Manager",
            prefWidth = 825.0,
            prefHeight = 750.0,
            isResizable = false,
            isPersistent = false
        ),
        AppScreen.IDE to WindowPolicy(
            title = "Nodex"
        )
    )

    private val overlayScreens = mapOf(
        OverlayScreen.SETTINGS to WindowPolicy(
            title = "Settings",
            prefWidth = 950.0,
            prefHeight = 750.0,
            isModal = true,
            isResizable = true
        )
    )

    /**
     * Returns the profile for a primary app screen.
     */
    fun getProfile(screen: AppScreen): WindowPolicy =
        appScreens[screen] ?: throw IllegalArgumentException("No profile found for screen: $screen")

    /**
     * Returns the profile for an overlay screen.
     */
    fun getProfile(overlay: OverlayScreen): WindowPolicy =
        overlayScreens[overlay] ?: throw IllegalArgumentException("No profile found for overlay: $overlay")
}
