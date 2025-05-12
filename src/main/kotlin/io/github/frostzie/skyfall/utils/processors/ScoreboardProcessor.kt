package io.github.frostzie.skyfall.utils.processors

import io.github.frostzie.skyfall.data.IslandType
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot

/**
 * Processes the scoreboard to extract useful information about the player's current state in Skyblock.
 * This utility processes the scoreboard data to determine location, game state, and other information.
 */
object ScoreboardProcessor {
    private val currentScoreboard = mutableListOf<String>()
    private var isInRift = false
    private var wasInRift = false
    private var isOnSkyblock = false
    private var wasOnSkyblock = false
    private var recentChange = 100
    private var timer = 20
    private var previousRegion: String? = null
    private var currentIsland: IslandType = IslandType.UNKNOWN

    /**
     * Updates the current state of the Scoreboard.
     * Should be called every tick to keep the data updated.
     */
    fun processScoreboard() {
        timer--
        if (timer > 0) return

        try {
            val client = MinecraftClient.getInstance()
            val player = client.player ?: return
            val scoreboard = player.scoreboard

            currentScoreboard.clear()

            val sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR) ?: return

            for (holder in scoreboard.knownScoreHolders) {
                if (scoreboard.getScoreHolderObjectives(holder).containsKey(sidebar)) {
                    val team = scoreboard.getScoreHolderTeam(holder.nameForScoreboard)
                    if (team != null) {
                        val lineText = team.prefix.string + team.suffix.string
                        currentScoreboard.add(lineText)
                    }
                }
            }
            updateLocationState()
            updateCurrentIsland()

            timer = 20
        } catch (e: Exception) {
        }
    }

    /**
     * Updates flags indicating whether the player is on Skyblock and/or in the Rift.
     */
    private fun updateLocationState() {
        var foundSkyblock = false
        var foundRift = false

        for (line in currentScoreboard) {
            if (line.contains("ф")) {
                foundRift = true
                foundSkyblock = true
                break
            }
            if (line.contains("⏣")) {
                foundSkyblock = true
            }
        }

        isInRift = foundRift
        isOnSkyblock = foundSkyblock
    }

    /**
     * Updates the current island based on scoreboard information.
     */
    private fun updateCurrentIsland() {
        val region = getRegion()
        if (region != previousRegion) {
            previousRegion = region
            currentIsland = determineIslandFromRegion(region)
        }
    }

    /**
     * Gets the current region text from the scoreboard.
     *
     * @return The region text, or null if not on Skyblock
     */
    fun getRegion(): String? {
        for (line in currentScoreboard) {
            if (line.contains("ф")) {
                return line
            }
            if (line.contains("⏣")) {
                return line
            }
        }
        return null
    }

    /**
     * Determines the island type from the region text.
     */
    private fun determineIslandFromRegion(regionText: String?): IslandType {
        if (regionText == null) return IslandType.UNKNOWN

        val locationPattern = if (isInRift) {
            "ф (.+)".toRegex()
        } else {
            "⏣ (.+)".toRegex()
        }

        val matchResult = locationPattern.find(regionText)
        val locationName = matchResult?.groupValues?.getOrNull(1)?.trim() ?: return IslandType.UNKNOWN

        return IslandType.fromDisplayName(locationName)
    }

    /**
     * Checks if there were changes in the player's region status.
     *
     * @return An array of two booleans: [skyblockChanged, riftChanged]
     */
    fun regionChange(): BooleanArray {
        val regionChanges = BooleanArray(2)
        regionChanges[0] = wasOnSkyblock != isOnSkyblock
        regionChanges[1] = wasInRift != isInRift

        if (recentChange <= 0) {
            wasInRift = isInRift
            wasOnSkyblock = isOnSkyblock
            recentChange = 100
        }
        recentChange--

        return regionChanges
    }

    /**
     * Checks if the player is currently in the Rift.
     *
     * @return True if the player is in the Rift, false otherwise
     */
    fun isInRift(): Boolean = isInRift

    /**
     * Checks if the player is currently on Skyblock.
     *
     * @return True if the player is on Skyblock, false otherwise
     */
    fun isOnSkyblock(): Boolean = isOnSkyblock

    /**
     * Gets the current island the player is on.
     *
     * @return The current Island as an IslandType
     */
    fun getCurrentIsland(): IslandType = currentIsland

    /**
     * Gets the full list of scoreboard lines.
     *
     * @return A list of all scoreboard lines
     */
    fun getScoreboard(): List<String> = currentScoreboard

    /**
     * Checks if the player is on a slayer quest.
     *
     * @return True if the player is on a slayer quest, false otherwise
     */
    fun onSlayerQuest(): Boolean {
        return currentScoreboard.any { it.contains("Slayer Quest") }
    }

    /**
     * Checks if a slayer boss has spawned.
     *
     * @return True if a slayer boss has spawned, false otherwise
     */
    fun bossSpawned(): Boolean {
        return currentScoreboard.any { it.contains("Slay the boss!") }
    }
}