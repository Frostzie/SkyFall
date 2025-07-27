package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.IslandChangeEvent
import io.github.frostzie.skyfall.events.RiftStateEvent
import io.github.frostzie.skyfall.events.ScoreboardUpdateEvent
import io.github.frostzie.skyfall.events.SkyblockStateEvent
import io.github.frostzie.skyfall.events.TabListUpdateEvent
import io.github.frostzie.skyfall.events.core.EventBus

// Taken from SkyHanni and modified
/**
 * The single source of truth for the player's current location in SkyBlock.
 * It listens to both Scoreboard and TabList events to make the most accurate
 * determination possible, prioritizing the tab list for island type.
 */
object IslandDetector {
    private val RIFT_SCOREBOARD_PATTERN = "ф (.+)".toRegex()
    private val NORMAL_SCOREBOARD_PATTERN = "⏣ (.+)".toRegex()
    private val TAB_AREA_PATTERN = "(?:Area|Dungeon):\\s*(.+)".toRegex()

    private var lastScoreboardRegion: String? = null
    private var lastTabListArea: String? = null
    private var lastIsInRift: Boolean = false

    private var currentIsland: IslandType = IslandType.UNKNOWN
    private var isOnSkyblock: Boolean = false
    private var isInRift: Boolean = false

    /**
     * Initializes the detector by listening for processor events.
     */
    fun init() {
        EventBus.listen(ScoreboardUpdateEvent::class.java) { event ->
            lastScoreboardRegion = event.region
            lastIsInRift = event.isInRift
            updateIslandState()
        }

        EventBus.listen(TabListUpdateEvent::class.java) { event ->
            lastTabListArea = event.area
            updateIslandState()
        }
    }

    /**
     * This is the core logic. It's called whenever new data is available from
     * either the scoreboard or tab list. It determines the new island state
     * and fires an event if it has changed.
     */
    private fun updateIslandState() {
        val newIsOnSkyblock = lastScoreboardRegion != null || lastTabListArea != null
        val newIsInRift = lastIsInRift // Rift is only detectable from the scoreboard symbol

        val tabListLocation = parseLocationFromTabList(lastTabListArea)
        val scoreboardLocation = parseLocationFromScoreboard(lastScoreboardRegion, newIsInRift)

        val finalLocationName = tabListLocation ?: scoreboardLocation
        val newIsland = IslandType.fromDisplayName(finalLocationName ?: "")

        fireStateChangeEvents(newIsland, newIsOnSkyblock, newIsInRift)
    }

    /**
     * Compares the new state with the old state and fires events if anything has changed.
     * This keeps the main update logic cleaner.
     */
    private fun fireStateChangeEvents(newIsland: IslandType, newIsOnSkyblock: Boolean, newIsInRift: Boolean) {
        if (newIsland == currentIsland && newIsOnSkyblock == isOnSkyblock && newIsInRift == isInRift) {
            return
        }

        val oldIsland = currentIsland
        val oldIsOnSkyblock = isOnSkyblock
        val oldIsInRift = isInRift

        currentIsland = newIsland
        isOnSkyblock = newIsOnSkyblock
        isInRift = newIsInRift

        if (newIsland != oldIsland) {
            IslandChangeEvent(oldIsland, newIsland).post()
        }
        if (newIsOnSkyblock != oldIsOnSkyblock) {
            SkyblockStateEvent(newIsOnSkyblock, oldIsOnSkyblock).post()
        }
        if (newIsInRift != oldIsInRift) {
            RiftStateEvent(newIsInRift, oldIsInRift).post()
        }
    }

    /**
     * Helper function to parse the location name from the scoreboard's region line.
     */
    private fun parseLocationFromScoreboard(regionText: String?, isInRift: Boolean): String? {
        if (regionText == null) return null
        val cleanRegionText = ColorUtils.stripColorCodes(regionText)
        val locationPattern = if (isInRift) RIFT_SCOREBOARD_PATTERN else NORMAL_SCOREBOARD_PATTERN
        return locationPattern.find(cleanRegionText)?.groupValues?.getOrNull(1)?.trim()
    }

    /**
     * Helper function to parse the location name from the tab list's area line.
     */
    private fun parseLocationFromTabList(areaText: String?): String? {
        if (areaText == null || areaText.contains("§cNo Area Found!")) return null
        val cleanAreaText = ColorUtils.stripColorCodes(areaText)
        return TAB_AREA_PATTERN.find(cleanAreaText)?.groupValues?.getOrNull(1)?.trim()
    }

    fun getCurrentIsland(): IslandType = currentIsland
    fun isOnIsland(island: IslandType): Boolean = currentIsland == island
    fun isOnSkyblock(): Boolean = isOnSkyblock
    fun isInRift(): Boolean = isInRift
}