package io.github.frostzie.skyfall.commands

import com.mojang.brigadier.context.CommandContext
import io.github.frostzie.skyfall.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient

//TODO: maybe change to /sf commands so doesn't conflict with other mods

object DungeonCommands {
    val clientCommand = ClientCommandRegistrationCallback.EVENT

    fun requeue() {
        clientCommand.register { a, _ ->
            val executeRequeue = { context: CommandContext<FabricClientCommandSource> ->
                val player = MinecraftClient.getInstance().player
                if (player != null) {
                    player.networkHandler.sendChatCommand("instancerequeue")
                    ChatUtils.messageToChat("§aRequeueing")
                }
                1
            }
            a.register(literal("requeue").executes(executeRequeue))
            a.register(literal("rq").executes(executeRequeue))
        }
    }

    fun floorCommands() {
            clientCommand.register { a, _ ->
                val floorOptions = mapOf(
                    // Normal Floors
                    "f1" to Pair("joindungeon catacombs 1", "§aJoining Floor 1"),
                    "f2" to Pair("joindungeon catacombs 2", "§aJoining Floor 2"),
                    "f3" to Pair("joindungeon catacombs 3", "§aJoining Floor 3"),
                    "f4" to Pair("joindungeon catacombs 4", "§aJoining Floor 4"),
                    "f5" to Pair("joindungeon catacombs 5", "§aJoining Floor 5"),
                    "f6" to Pair("joindungeon catacombs 6", "§aJoining Floor 6"),
                    "f7" to Pair("joindungeon catacombs 7", "§aJoining Floor 7"),
                    // Master mode Floors
                    "m1" to Pair("joindungeon master_catacombs 1", "§aJoining Master Mode 1"),
                    "m2" to Pair("joindungeon master_catacombs 2", "§aJoining Master Mode 2"),
                    "m3" to Pair("joindungeon master_catacombs 3", "§aJoining Master Mode 3"),
                    "m4" to Pair("joindungeon master_catacombs 4", "§aJoining Master Mode 4"),
                    "m5" to Pair("joindungeon master_catacombs 5", "§aJoining Master Mode 5"),
                    "m6" to Pair("joindungeon master_catacombs 6", "§aJoining Master Mode 6"),
                    "m7" to Pair("joindungeon master_catacombs 7", "§aJoining Master Mode 7")
                )

                floorOptions.forEach { (commandName, commandData) ->
                    val (serverCommand, message) = commandData

                    a.register(literal(commandName).executes { context ->
                        val player = MinecraftClient.getInstance().player
                        if (player != null) {
                            player.networkHandler.sendChatCommand(serverCommand)
                            ChatUtils.messageToChat(message)
                        }
                        1
                    })
                }
            }
        }
    }