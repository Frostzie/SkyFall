package io.github.frostzie.nodex.services.ui

import io.github.frostzie.nodex.domain.uicontract.AppScreen
import io.github.frostzie.nodex.domain.uicontract.OverlayScreen
import io.github.frostzie.nodex.services.core.ConcurrencyService
import io.github.frostzie.nodex.utils.LoggerProvider
import javafx.beans.property.SimpleObjectProperty

/**
 * Manages the global navigation states.
 *
 * Tracks the currently active [AppScreen] and provides a way to
 * transition between primary views.
 * Also manages the active [OverlayScreen].
 * State changes are assigned to the UI thread.
 */
class NavigationService(private val concurrencyService: ConcurrencyService) {
    private val logger = LoggerProvider.getLogger("NavigationService")

    /**
     * The currently active primary screen.
     * ViewModels should bind to this property to react to navigation changes.
     */
    val currentScreen = SimpleObjectProperty(AppScreen.INTRO)

    /**
     * The primary screen that was active before the current one.
     */
    var previousScreen: AppScreen = AppScreen.INTRO
        private set

    /**
     * The currently active overlay, or null if no overlay is shown.
     */
    val activeOverlay = SimpleObjectProperty<OverlayScreen?>(null)

    fun initialize(initialScreen: AppScreen) {
        currentScreen.set(initialScreen)
        
        currentScreen.addListener { _, old, new ->
            if (old != new) {
                logger.debug("Screen switch: {} -> {}", old, new)
            }
        }

        logger.debug("NavigationService initialized. Screen: {}", initialScreen)
    }
    
    /**
     * Switch the current screen to a specified screen.
     */
    fun navigateTo(screen: AppScreen) {
        concurrencyService.runOnUI {
            val current = currentScreen.get()
            if (current != screen) {
                previousScreen = current
                currentScreen.set(screen)
            }
        }
    }

    /**
     * Open the specified overlay.
     */
    fun showOverlay(overlay: OverlayScreen) {
        concurrencyService.runOnUI {
            if (activeOverlay.get() != overlay) {
                activeOverlay.set(overlay)
            }
        }
    }

    /**
     * Close the currently active overlay.
     */
    fun closeOverlay() {
        concurrencyService.runOnUI {
            if (activeOverlay.get() != null) {
                activeOverlay.set(null)
            }
        }
    }
}