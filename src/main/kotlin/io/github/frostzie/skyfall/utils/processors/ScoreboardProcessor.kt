package io.github.frostzie.skyfall.utils.processors

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.IslandChangeEvent
import io.github.frostzie.skyfall.events.RiftStateEvent
import io.github.frostzie.skyfall.events.ScoreboardUpdateEvent
import io.github.frostzie.skyfall.events.SkyblockStateEvent
import io.github.frostzie.skyfall.events.SlayerQuestEvent
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.LoggerProvider
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot

// Taken and modified from Skyhanni
/**
 * A simplified and robust event-driven scoreboard processor. It detects changes
 * reliably and fires events from Event.kt when the state changes.
 */
object ScoreboardProcessor {
    private var previousScoreboardLines = listOf<String>()
    private var previousIsland: IslandType = IslandType.UNKNOWN
    private var previousIsOnSkyblock = false
    private var previousIsInRift = false
    private var previousIsOnSlayerQuest = false
    private var previousIsBossSpawned = false

    private val logger = LoggerProvider.getLogger("ScoreboardProcessor")

    /**
     * Processes the scoreboard on every tick and fires events when state changes.
     * This is the main entry point called from SkyFall.kt.
     */
    fun processScoreboard() {
        try {
            val client = MinecraftClient.getInstance()
            val world = client.world ?: return
            val scoreboard = world.scoreboard
            val sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)

            if (sidebar == null) {
                if (previousScoreboardLines.isNotEmpty()) {
                    processChanges(emptyList())
                }
                return
            }

            val newUnsortedLines = mutableListOf<String>()
            for (holder in scoreboard.knownScoreHolders) {
                if (scoreboard.getScoreHolderObjectives(holder).containsKey(sidebar)) {
                    val team = scoreboard.getScoreHolderTeam(holder.nameForScoreboard)
                    if (team != null) {
                        val prefix = team.prefix?.string ?: ""
                        val suffix = team.suffix?.string ?: ""
                        newUnsortedLines.add(prefix + suffix)
                    }
                }
            }

            val newScoreboardLines = newUnsortedLines.sorted()

            if (newScoreboardLines == previousScoreboardLines) {
                return
            }

            processChanges(newScoreboardLines)

        } catch (e: Exception) {
            logger.error("Error processing scoreboard: ${e.message}", e)
        }
    }

    /**
     * Calculates the new state, compares it to the previous state, and fires all necessary events.
     */
    private fun processChanges(newLines: List<String>) {
        val newIsOnSkyblock = newLines.any { it.contains("⏣") || it.contains("ф") }
        val newIsInRift = newLines.any { it.contains("ф") }
        val newRegion = newLines.find { it.contains("ф") || it.contains("⏣") }
        val newIsland = determineIslandFromRegion(newRegion, newIsInRift)
        val newIsOnSlayerQuest = newLines.any { it.contains("Slayer Quest") }
        val newIsBossSpawned = newLines.any { it.contains("Slay the boss!") }

        if (newIsOnSkyblock != previousIsOnSkyblock) {
            SkyblockStateEvent(newIsOnSkyblock, previousIsOnSkyblock).post()
        }
        if (newIsInRift != previousIsInRift) {
            RiftStateEvent(newIsInRift, previousIsInRift).post()
        }
        if (newIsland != previousIsland) {
            IslandChangeEvent(previousIsland, newIsland).post()
        }
        if (newIsOnSlayerQuest != previousIsOnSlayerQuest || newIsBossSpawned != previousIsBossSpawned) {
            SlayerQuestEvent(newIsOnSlayerQuest, newIsBossSpawned).post()
        }

        ScoreboardUpdateEvent(
            newLines,
            newIsOnSkyblock,
            newIsInRift,
            newIsland,
            newRegion
        ).post()

        previousScoreboardLines = newLines
        previousIsland = newIsland
        previousIsOnSkyblock = newIsOnSkyblock
        previousIsInRift = newIsInRift
        previousIsOnSlayerQuest = newIsOnSlayerQuest
        previousIsBossSpawned = newIsBossSpawned
    }

    /**
     * Determines the island type from the region line of the scoreboard.
     */
    private fun determineIslandFromRegion(regionText: String?, isInRift: Boolean): IslandType {
        if (regionText == null) return IslandType.UNKNOWN

        val cleanRegionText = ColorUtils.stripColorCodes(regionText)
        val locationPattern = if (isInRift) "ф (.+)".toRegex() else "⏣ (.+)".toRegex()

        val locationName = locationPattern.find(cleanRegionText)?.groupValues?.getOrNull(1)?.trim()
        return IslandType.fromDisplayName(locationName ?: "")
    }

    fun getRegion(): String? = previousScoreboardLines.find { it.contains("ф") || it.contains("⏣") }
    fun getCurrentIsland(): IslandType = previousIsland
    fun isOnSkyblock(): Boolean = previousIsOnSkyblock
    fun isInRift(): Boolean = previousIsInRift
    fun getScoreboard(): List<String> = previousScoreboardLines
}