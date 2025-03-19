package io.github.frostzie.skyfall.commands

import io.github.frostzie.skyfall.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient

object DungeonCommands {

    val Command = ClientCommandRegistrationCallback.EVENT
    val Execute = MinecraftClient.getInstance().player?.networkHandler

    fun requeue() {
        Command.register { a, _ ->
            a.register(literal("requeue").executes {
                Execute?.sendChatCommand("instancerequeue")
                0
            })
        }
    }

    //TODO: add a toggle to turn these chat commands on and off
    //TODO: fix not actually sending the commands... -- to late to do rn
    fun shortening() {
        Command.register { a, _ ->
            // Normal Floors
            a.register(literal("f1").executes {
                Execute?.sendChatCommand("joindungeon catacombs 1")
                ChatUtils.messageToChat("§aJoining Floor 1")
                0
            })
            a.register(literal("f2").executes {
                Execute?.sendChatCommand("joindungeon catacombs 2")
                ChatUtils.messageToChat("§aJoining Floor 2")
                0
            })
            a.register(literal("f3").executes {
                Execute?.sendChatCommand("joindungeon catacombs 3")
                ChatUtils.messageToChat("§aJoining Floor 3")
                0
            })
            a.register(literal("f4").executes {
                Execute?.sendChatCommand("joindungeon catacombs 4")
                ChatUtils.messageToChat("§aJoining Floor 4")
                0
            })
            a.register(literal("f5").executes {
                Execute?.sendChatCommand("joindungeon catacombs 5")
                ChatUtils.messageToChat("§aJoining Floor 5")
                0
            })
            a.register(literal("f6").executes {
                Execute?.sendChatCommand("joindungeon catacombs 6")
                ChatUtils.messageToChat("§aJoining Floor 6")
                0
            })
            a.register(literal("f7").executes {
                Execute?.sendChatCommand("joindungeon catacombs 7")
                ChatUtils.messageToChat("§aJoining Floor 7")
                0
            })
            // Master Mode Floors
            a.register(literal("m1").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 1")
                ChatUtils.messageToChat("§aJoining Master Mode 1")
                0
            })
            a.register(literal("m2").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 2")
                ChatUtils.messageToChat("§aJoining Master Mode 2")
                0
            })
            a.register(literal("m3").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 3")
                ChatUtils.messageToChat("§aJoining Master Mode 3")
                0
            })
            a.register(literal("m4").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 4")
                ChatUtils.messageToChat("§aJoining Master Mode 4")
                0
            })
            a.register(literal("m5").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 5")
                ChatUtils.messageToChat("§aJoining Master Mode 5")
                0
            })
            a.register(literal("m6").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 6")
                ChatUtils.messageToChat("§aJoining Master Mode 6")
                0
            })
            a.register(literal("m7").executes {
                Execute?.sendChatCommand("joindungeon master_catacombs 7")
                ChatUtils.messageToChat("§aJoining Master Mode 7")
                0
            })
        }
    }
}