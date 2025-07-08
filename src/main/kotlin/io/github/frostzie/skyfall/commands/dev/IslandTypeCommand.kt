package io.github.frostzie.skyfall.commands.dev

import com.mojang.brigadier.context.CommandContext
import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.ColorUtils
import io.github.frostzie.skyfall.utils.CommandUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.processors.ScoreboardProcessor
import io.github.frostzie.skyfall.utils.processors.TabListProcessor
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

private val devConfig get() = SkyFall.feature.dev

object IslandTypeCommand {
    fun register() {
        if (devConfig.enabledDevMode && devConfig.locationCommand) {
            ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
                dispatcher.register(CommandUtils.caseInsensitiveLiteral("sfDevIslandType").executes { context ->
                    executeCommand(context)
                })
                dispatcher.register(CommandUtils.caseInsensitiveLiteral("skyfallDevIslandType").executes { context ->
                    executeCommand(context)
                })
            }
        }
    }

    private fun executeCommand(context: CommandContext<FabricClientCommandSource>): Int {
        try {
            val currentIsland = IslandDetector.getCurrentIsland()
            val isOnSkyblock = IslandDetector.isOnSkyblock()
            val isInRift = IslandDetector.isInRift()

            val scoreboardRegion = ScoreboardProcessor.getRegion()
            val tabListArea = TabListProcessor.getArea()
            val tabListProfile = TabListProcessor.getProfile()

            val rawScoreboardLocation = extractLocationFromScoreboard(scoreboardRegion, isInRift)
            val rawTabListLocation = extractLocationFromTabList(tabListArea)

            ChatUtils.messageToChat("§6§l=== Dev Island Type Detection ===")
            ChatUtils.messageToChat("§7Final State (from IslandDetector):")
            ChatUtils.messageToChat("§8- §eOn Skyblock: §${if (isOnSkyblock) "a" else "c"}$isOnSkyblock")
            ChatUtils.messageToChat("§8- §eIn Rift: §${if (isInRift) "a" else "c"}$isInRift")
            ChatUtils.messageToChat("§8- §eDetected Island: §b${currentIsland.displayName}")

            ChatUtils.messageToChat("")
            ChatUtils.messageToChat("§7Raw Data (from Processors):")
            ChatUtils.messageToChat("§8- §eScoreboard Region: §f${scoreboardRegion ?: "§cNot Found"}")
            rawScoreboardLocation?.let {
                ChatUtils.messageToChat("§8  §7→ Parsed Area (Scoreboard): §a$it").copyContent(it).send()
            }
            ChatUtils.messageToChat("§8- §eTab List Area: §f$tabListArea")
            rawTabListLocation?.let {
                ChatUtils.messageToChat("§8  §7→ Parsed Island (Tab): §a$it").copyContent(it).send()
            }
            ChatUtils.messageToChat("§8- §eTab List Profile: §f$tabListProfile")

            ChatUtils.messageToChat("")
            ChatUtils.messageToChat("§7Island Type Matching:")
            if (currentIsland == IslandType.UNKNOWN) {
                ChatUtils.messageToChat("§c§l⚠ Unknown Island Detected!")
                } else {
                ChatUtils.messageToChat("§a§l✓ Island Recognized")
                ChatUtils.messageToChat("§8- §eEnum Value: §d${currentIsland.name}")
                ChatUtils.messageToChat("§8- §eDisplay Name: §b${currentIsland.displayName}").copyContent(currentIsland.displayName).send()
            }

            ChatUtils.messageToChat("§6§l===============================")

        } catch (e: Exception) {
            ChatUtils.error("Failed to execute dev island type command: ${e.message}", e.toString())
        }

        return 1
    }

    private fun extractLocationFromScoreboard(regionText: String?, isInRift: Boolean): String? {
        if (regionText == null) return null
        val cleanRegionText = ColorUtils.stripColorCodes(regionText)
        val locationPattern = if (isInRift) "ф (.+)".toRegex() else "⏣ (.+)".toRegex()
        return locationPattern.find(cleanRegionText)?.groupValues?.getOrNull(1)?.trim()
    }

    private fun extractLocationFromTabList(areaText: String?): String? {
        if (areaText == null || areaText.contains("§cNo Area Found!")) return null
        val cleanAreaText = ColorUtils.stripColorCodes(areaText)
        val areaPattern = "(?:Area|Dungeon):\\s*(.+)".toRegex()
        return areaPattern.find(cleanAreaText)?.groupValues?.getOrNull(1)?.trim()
    }
}