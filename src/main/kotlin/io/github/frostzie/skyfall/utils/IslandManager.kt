package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.events.CurrentIslandEvents
import io.github.frostzie.skyfall.utils.events.CurrentIslandListener
import io.github.frostzie.skyfall.utils.events.IslandChangeEvent
import io.github.frostzie.skyfall.utils.events.IslandChangeListener
import io.github.frostzie.skyfall.utils.events.IslandEvents

// Taken from SkyHanni
/**
 * Manager class for island-related functionality.
 * Initializes the island detection system and provides high-level methods to interact with it.
 */
object IslandManager {
    private val logger = LoggerProvider.getLogger("IslandManager")

    /**
     * Initializes the Island Manager.
     * Sets up the island detection system and registers default listeners.
     */
    fun init() {
        logger.info("Initializing Island Manager")
        IslandDetector.init()
        if (System.getProperty("skyfall.debug") == "true") {
            registerDebugListener()
        }
    }

    /**
     * Gets the current island the player is on.
     *
     * @return The current Island as an IslandType
     */
    fun getCurrentIsland(): IslandType = IslandDetector.getCurrentIsland()

    /**
     * Checks if the player is currently on Skyblock.
     *
     * @return True if the player is on Skyblock, false otherwise
     */
    fun isOnSkyblock(): Boolean = IslandDetector.isOnSkyblock()

    /**
     * Checks if the player is currently in the Rift.
     *
     * @return True if the player is in the Rift, false otherwise
     */
    fun isInRift(): Boolean = IslandDetector.isInRift()

    /**
     * Checks if the player is on a specific island.
     *
     * @param islandType The island type to check for
     * @return True if the player is on the specified island, false otherwise
     */
    fun isOnIsland(islandType: IslandType): Boolean = IslandDetector.isOnIsland(islandType)

    /**
     * Checks if the player is on one of the specified islands.
     *
     * @param islandTypes The island types to check for
     * @return True if the player is on one of the specified islands, false otherwise
     */
    fun isOnOneOf(vararg islandTypes: IslandType): Boolean = IslandDetector.isOnOneOf(*islandTypes)

    /**
     * Checks if a feature should be active on the current island.
     *
     * @param requiredIsland The island where the feature should be active
     * @return True if the current island matches the required island, false otherwise
     */
    fun shouldFeatureBeActive(requiredIsland: IslandType): Boolean {
        return CurrentIslandEvents.isOnRequiredIsland(requiredIsland, getCurrentIsland())
    }

    /**
     * Checks if a feature should be active on any of the specified islands.
     *
     * @param islandTypes The island types where the feature should be active
     * @return True if the current island is one of the specified islands, false otherwise
     */
    fun shouldFeatureBeActiveOnAny(vararg islandTypes: IslandType): Boolean {
        return getCurrentIsland().isOneOf(*islandTypes)
    }

    /**
     * Registers a listener to be notified when the island changes.
     *
     * @param listener The listener to register
     */
    fun registerIslandChangeListener(listener: IslandChangeListener) {
        IslandEvents.registerIslandChangeListener(listener)
    }

    /**
     * Unregisters an island change listener.
     *
     * @param listener The listener to unregister
     */
    fun unregisterIslandChangeListener(listener: IslandChangeListener) {
        IslandEvents.unregisterIslandChangeListener(listener)
    }

    /**
     * Registers a current island listener.
     *
     * @param listener The listener to register
     */
    fun registerCurrentIslandListener(listener: CurrentIslandListener) {
        CurrentIslandEvents.registerCurrentIslandListener(listener)
    }

    /**
     * Unregisters a current island listener.
     *
     * @param listener The listener to unregister
     */
    fun unregisterCurrentIslandListener(listener: CurrentIslandListener) {
        CurrentIslandEvents.unregisterCurrentIslandListener(listener)
    }

    /**
     * Registers a debug listener that prints island changes to chat.
     * Only used when debug mode is enabled.
     */
    private fun registerDebugListener() {
        IslandEvents.registerIslandChangeListener(object : IslandChangeListener {
            override fun onIslandChange(event: IslandChangeEvent) {
                ChatUtils.messageToChat("§3§lSkyFall§r §8» §eIsland changed from §b${event.oldIsland.displayName}§e to §b${event.newIsland.displayName}")
            }
        })
    }
}