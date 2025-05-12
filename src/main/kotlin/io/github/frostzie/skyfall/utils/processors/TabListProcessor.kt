package io.github.frostzie.skyfall.utils.processors

import io.github.frostzie.skyfall.mixin.TabListAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.PlayerListEntry
import org.slf4j.LoggerFactory
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

/**
 * Processes the tab list to extract useful information about the player's current state in Skyblock.
 * This utility extracts area, profile, and other information from the tab list entries.
 */
object TabListProcessor {
    private val logger = LoggerFactory.getLogger("skyfall:TabListProcessor")
    private val tabList = mutableListOf<String>()
    private val previousPlayers = mutableSetOf<String>()
    private val currentValues = HashMap<String, String?>()
    private var hasChanged = true
    private val errorCount = ConcurrentHashMap<String, Int>()

    /**
     * Updates the current state of the Tab List.
     * Should be called every tick to keep the data updated.
     */
    fun processTabList() {
        try {
            val client = MinecraftClient.getInstance()
            val networkHandler = client.networkHandler ?: return
            val players = try {
                (networkHandler as TabListAccessor).playerList.toSet()
            } catch (e: Exception) {
                logger.error("Failed to access player list: ${e.message}")
                emptySet()
            }

            val currentPlayerFingerprints = players.mapNotNull { player ->
                try {
                    player.profile.name
                } catch (e: Exception) {
                    logger.debug("Could not get identifier for player: ${e.message}")
                    null
                }
            }.toSet()

            if (currentPlayerFingerprints == previousPlayers) {
                hasChanged = false
                return
            }

            hasChanged = true
            tabList.clear()

            players.forEach { player ->
                processPlayerEntry(player)
            }

            previousPlayers.clear()
            previousPlayers.addAll(currentPlayerFingerprints)

            getArea()
            getProfile()
        } catch (e: Exception) {
            logger.error("Error in processTabList: ${e.message}")
        }
    }

    /**
     * Safely processes a player entry to extract display name.
     */
    private fun processPlayerEntry(player: PlayerListEntry) {
        try {
            val displayName = try {
                player.displayName?.string ?: player.profile.name
            } catch (e: Exception) {
                try {
                    player.profile.name
                } catch (e2: Exception) {
                    val identifier = try {
                        "Player-${player.hashCode()}"
                    } catch (e3: Exception) {
                        "Unknown-${System.nanoTime()}"
                    }

                    val errorKey = "player-${identifier}"
                    val count = errorCount.compute(errorKey) { _, v -> (v ?: 0) + 1 } ?: 1

                    if (count <= 3) {
                        logger.debug("Could not get display name for player (attempt $count): ${e.message}")
                    }

                    "Unknown Player"
                }
            }
            tabList.add(displayName)
        } catch (e: Exception) {
            logger.debug("Error processing player in tab list: ${e.message}")
        }
    }

    /**
     * Gets the area information from the tab list.
     *
     * @return The area text, or a default message if not found
     */
    fun getArea(): String {
        if (!hasChanged) return currentValues["Area"] ?: "§cNo Area Found!"

        for (entry in tabList) {
            if (entry.contains("Area:") || entry.contains("Dungeon:")) {
                currentValues["Area"] = entry
                return entry
            }
        }

        currentValues["Area"] = "§cNo Area Found!"
        return "§cNo Area Found!"
    }

    /**
     * Gets the profile information from the tab list.
     *
     * @return The profile text, or a default message if not found
     */
    fun getProfile(): String {
        if (!hasChanged) return currentValues["Profile"] ?: "§cNo Profile Found!"

        for (entry in tabList) {
            if (entry.contains("Profile:")) {
                currentValues["Profile"] = entry
                return entry
            }
        }

        currentValues["Profile"] = "§cNo Profile Found!"
        return "§cNo Profile Found!"
    }

    /**
     * Gets the mana information from the tab list.
     *
     * @return The mana text, or a default message if not found
     */
    fun getMana(): String {
        if (!hasChanged) return currentValues["Mana"] ?: "§cMana not found. Try enabling your Mana /tablist widget."

        for (entry in tabList) {
            if (entry.contains("Mana:")) {
                currentValues["Mana"] = entry
                return entry
            }
        }

        currentValues["Mana"] = "§cMana not found. Try enabling your Mana /tablist widget."
        return "§cMana not found. Try enabling your Mana /tablist widget."
    }

    /**
     * Gets the soulflow information from the tab list.
     *
     * @return The soulflow text, or a default message if not found
     */
    fun getSoulflow(): String {
        if (!hasChanged) return currentValues["Soulflow"] ?: "§cSoulflow not found. Try enabling your Soulflow /tablist widget."

        for (entry in tabList) {
            if (entry.contains("Soulflow:")) {
                currentValues["Soulflow"] = entry
                return entry
            }
        }

        currentValues["Soulflow"] = "§cSoulflow not found. Try enabling your Soulflow /tablist widget."
        return "§cSoulflow not found. Try enabling your Soulflow /tablist widget."
    }

    /**
     * Extracts a value from a tab list entry based on a label.
     *
     * @param line The tab list line to process
     * @param label The label to search for (e.g., "Area:", "Profile:")
     * @return The extracted value, or null if not found
     */
    private fun extractValue(line: String, label: String): String? {
        val regex = "$label\\s*(.+)".toRegex()
        val matchResult = regex.find(line)
        return matchResult?.groupValues?.getOrNull(1)?.trim()
    }

    /**
     * Gets the full tab list.
     *
     * @return A list of all tab list entries
     */
    fun getTabList(): List<String> = tabList
}