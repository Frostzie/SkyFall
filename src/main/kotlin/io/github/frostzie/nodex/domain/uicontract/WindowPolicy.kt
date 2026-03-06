package io.github.frostzie.nodex.domain.uicontract

/**
 * Defines the behavioral for a window (Stage).
 */
data class WindowPolicy(
    val title: String,
    val minWidth: Double = 600.0,
    val minHeight: Double = 400.0,
    val isResizable: Boolean = true,
    val isModal: Boolean = false,
    val alwaysOnTop: Boolean = false
) {
    companion object {
        /**
         * Returns the window policy for a primary screen.
         */
        fun forScreen(screen: AppScreen): WindowPolicy = when (screen) {
            AppScreen.INTRO -> WindowPolicy(
                title = "Nodex - Welcome",
                isResizable = false
            )

            AppScreen.PROJECT_MANAGER -> WindowPolicy(
                title = "Nodex - Project Manager"
            )

            AppScreen.IDE -> WindowPolicy(
                title = "Nodex"
            )
        }

        /**
         * Returns the window policy for an overlay.
         */
        fun forOverlay(overlay: OverlayScreen): WindowPolicy = when (overlay) {
            OverlayScreen.SETTINGS -> WindowPolicy(
                title = "Settings",
                isModal = true,
                isResizable = true
            )
        }
    }
}
