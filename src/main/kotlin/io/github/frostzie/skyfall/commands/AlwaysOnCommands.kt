package io.github.frostzie.skyfall.commands

import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.frostzie.skyfall.utils.ChatUtils
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object AlwaysOnCommands {

    val Command = ClientCommandRegistrationCallback.EVENT

    fun openConfig() {
        Command.register { a, _ ->
            a.register(literal("sf").executes {
                ConfigGuiManager.openConfigGui()
                1
            })
            a.register(literal("skyfall").executes {
                ConfigGuiManager.openConfigGui()
                1
            })
        }
    }

    fun sfColor() {
        Command.register { a, _ ->
            a.register(literal("sfcolor").executes {
                ChatUtils.messageToChat(
                    "§c===================================================\n" +
                            "§f&0 = §0Black             §f&1 = §1Dark Blue\n" +
                            "§f&2 = §2Dark Green     §f&3 = §3Dark Aqua\n" +
                            "§f&4 = §4Dark Red        §f&5 = §5Dark Purple\n" +
                            "§f&6 = §6Gold              §f&7 = §7Gray\n" +
                            "§f&8 = §8Dark Gray      §f&9 = §9Blue\n" +
                            "§f&a = §aGreen           §f&b = §bAqua\n" +
                            "§f&c = §cRed              §f&d = §dLight Purple\n" +
                            "§f&e = §eYellow           §f&f = §fWhite\n" +
                    "§c================= Formatting Codes ==================\n" +
                            "§f&k = Obfuscated (like this: §khellspawn§r§f)\n" +
                            "§f&l = §lBold          §r&m = §mStrikethrough \n" +
                            "§f&o = §oItalic            §f&n = §nUnderline\n" +
                            "§f&r = Reset\n" +
                    "§c==================================================="
                ).send()
                1
            })
        }
    }
}