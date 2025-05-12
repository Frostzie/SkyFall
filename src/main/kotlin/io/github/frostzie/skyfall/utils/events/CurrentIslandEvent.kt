package io.github.frostzie.skyfall.utils.events

import io.github.frostzie.skyfall.data.IslandType

/**
 * Event fired to query the current island.
 * This can be used by features to check if they should be active on the current island.
 *
 * @param island The current island the player is on
 */
data class CurrentIslandEvent(val island: IslandType)

/**
 * Interface for classes that want to listen to current island queries.
 */
interface CurrentIslandListener {
    /**
     * Called when a feature wants to check the current island.
     *
     * @param event The current island event containing information about the player's location
     * @return True if the feature should be active on this island, false otherwise
     */
    fun onCurrentIslandQuery(event: CurrentIslandEvent): Boolean
}

/**
 * Extension of the IslandEvents manager to handle current island queries.
 */
object CurrentIslandEvents {
    private val currentIslandListeners = mutableListOf<CurrentIslandListener>()

    /**
     * Registers a current island listener.
     *
     * @param listener The listener to register
     */
    fun registerCurrentIslandListener(listener: CurrentIslandListener) {
        if (!currentIslandListeners.contains(listener)) {
            currentIslandListeners.add(listener)
        }
    }

    /**
     * Unregisters a current island listener.
     *
     * @param listener The listener to unregister
     */
    fun unregisterCurrentIslandListener(listener: CurrentIslandListener) {
        currentIslandListeners.remove(listener)
    }

    /**
     * Checks if any feature should be active on the given island.
     *
     * @param island The current island to check
     * @return True if at least one feature should be active, false otherwise
     */
    fun checkCurrentIsland(island: IslandType): Boolean {
        val event = CurrentIslandEvent(island)
        return currentIslandListeners.any { listener ->
            try {
                listener.onCurrentIslandQuery(event)
            } catch (e: Exception) {
                println("Error in current island listener: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    /**
     * Checks if a specific feature should be active on the current island.
     *
     * @param requiredIsland The island where the feature should be active
     * @param currentIsland The current island the player is on
     * @return True if the current island matches the required island, false otherwise
     */
    fun isOnRequiredIsland(requiredIsland: IslandType, currentIsland: IslandType): Boolean {
        return currentIsland == requiredIsland
    }
}