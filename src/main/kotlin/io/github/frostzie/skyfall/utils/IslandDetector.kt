package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.events.IslandEvents.fireIslandChangeEvent
import io.github.frostzie.skyfall.utils.processors.ScoreboardProcessor
import io.github.frostzie.skyfall.utils.processors.TabListProcessor
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents

// Taken from SkyHanni
/**
 * Utility class for detecting the current island the player is on in Skyblock.
 * Combines information from both the scoreboard and tab list to provide accurate location information.
 */
object IslandDetector {
    private val logger = LoggerProvider.getLogger("IslandDetector")

    private var currentIsland: IslandType = IslandType.UNKNOWN
    private var previousIsland: IslandType = IslandType.UNKNOWN
    private var isOnSkyblock: Boolean = false
    private var isInRift: Boolean = false
    private var islandChangeListeners = mutableListOf<(IslandType, IslandType) -> Unit>()

    /**
     * Initializes the Island Detector.
     * Sets up the tick event to process scoreboard and tab list data.
     */
    fun init() {
        logger.info("Initializing Island Detector")
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.player == null) return@register

            ScoreboardProcessor.processScoreboard()
            TabListProcessor.processTabList()

            updateIslandInfo()
        }
    }

    /**
     * Updates the current island information based on scoreboard and tab list data.
     * Fires island change events if the island has changed.
     */
    private fun updateIslandInfo() {
        isOnSkyblock = ScoreboardProcessor.isOnSkyblock()
        isInRift = ScoreboardProcessor.isInRift()

        currentIsland = if (!isOnSkyblock) {
            IslandType.UNKNOWN
        } else {
            val scoreboardIsland = ScoreboardProcessor.getCurrentIsland()
            if (scoreboardIsland == IslandType.UNKNOWN) {
                determineIslandFromTabList()
            } else {
                scoreboardIsland
            }
        }

        if (currentIsland != previousIsland) {
            val oldIsland = previousIsland
            previousIsland = currentIsland

            islandChangeListeners.forEach { it(oldIsland, currentIsland) }

            fireIslandChangeEvent(oldIsland, currentIsland)

            logger.debug("Island changed from ${oldIsland.displayName} to ${currentIsland.displayName}")
        }
    }

    /**
     * Attempts to determine the island from the tab list area information.
     *
     * @return The determined island type, or UNKNOWN if it couldn't be determined
     */
    private fun determineIslandFromTabList(): IslandType {
        val areaText = TabListProcessor.getArea()
        val areaPattern = "Area:\\s*(.+)".toRegex()
        val matchResult = areaPattern.find(areaText)
        val areaName = matchResult?.groupValues?.getOrNull(1)?.trim() ?: return IslandType.UNKNOWN

        return IslandType.fromDisplayName(areaName)
    }

    /**
     * Gets the current island the player is on.
     *
     * @return The current Island as an IslandType
     */
    fun getCurrentIsland(): IslandType = currentIsland

    /**
     * Checks if the player is currently on Skyblock.
     *
     * @return True if the player is on Skyblock, false otherwise
     */
    fun isOnSkyblock(): Boolean = isOnSkyblock

    /**
     * Checks if the player is currently in the Rift.
     *
     * @return True if the player is in the Rift, false otherwise
     */
    fun isInRift(): Boolean = isInRift

    /**
     * Checks if the player is on a specific island.
     *
     * @param islandType The island type to check for
     * @return True if the player is on the specified island, false otherwise
     */
    fun isOnIsland(islandType: IslandType): Boolean = currentIsland == islandType

    /**
     * Checks if the player is on one of the specified islands.
     *
     * @param islandTypes The island types to check for
     * @return True if the player is on one of the specified islands, false otherwise
     */
    fun isOnOneOf(vararg islandTypes: IslandType): Boolean = currentIsland.isOneOf(*islandTypes)

    /**
     * Adds a listener to be notified when the island changes.
     *
     * @param listener A function that takes the old and new island as parameters
     */
    fun addIslandChangeListener(listener: (IslandType, IslandType) -> Unit) {
        islandChangeListeners.add(listener)
    }

    /**
     * Removes an island change listener.
     *
     * @param listener The listener to remove
     */
    fun removeIslandChangeListener(listener: (IslandType, IslandType) -> Unit) {
        islandChangeListeners.remove(listener)
    }
}