package io.github.frostzie.skyfall.utils.events

import io.github.frostzie.skyfall.data.IslandType

/**
 * Event fired when the player's island changes.
 *
 * @param oldIsland The island the player was previously on
 * @param newIsland The island the player is now on
 */
data class IslandChangeEvent(val oldIsland: IslandType, val newIsland: IslandType)

/**
 * Interface for classes that want to listen to island change events.
 */
interface IslandChangeListener {
    /**
     * Called when an island change is detected.
     *
     * @param event The island change event containing old and new island information
     */
    fun onIslandChange(event: IslandChangeEvent)
}

/**
 * Manager for island-related events.
 * Provides methods to register and unregister listeners, and to fire events.
 */
object IslandEvents {
    private val islandChangeListeners = mutableListOf<IslandChangeListener>()

    /**
     * Registers an island change listener.
     *
     * @param listener The listener to register
     */
    fun registerIslandChangeListener(listener: IslandChangeListener) {
        if (!islandChangeListeners.contains(listener)) {
            islandChangeListeners.add(listener)
        }
    }

    /**
     * Unregisters an island change listener.
     *
     * @param listener The listener to unregister
     */
    fun unregisterIslandChangeListener(listener: IslandChangeListener) {
        islandChangeListeners.remove(listener)
    }

    /**
     * Fires an island change event to all registered listeners.
     *
     * @param oldIsland The island the player was previously on
     * @param newIsland The island the player is now on
     */
    internal fun fireIslandChangeEvent(oldIsland: IslandType, newIsland: IslandType) {
        val event = IslandChangeEvent(oldIsland, newIsland)
        islandChangeListeners.forEach { listener ->
            try {
                listener.onIslandChange(event)
            } catch (e: Exception) {
                println("Error in island change listener: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}