package io.github.frostzie.skyfall.commands

import io.github.frostzie.skyfall.config.gui.ConfigGuiManager
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

    fun sfHelp() {          //TODO: Later on improve the look of this and add all the other cmds that come along
        Command.register { a, _ ->
            a.register(literal("sfhelp").executes {
                ChatUtils.messageToChat(
                    "§9---------------------------------------------------\n" +
                            "§f/sf - Open the SkyFall config GUI\n" +
                            "§f/sfcolor - Display the color codes\n" +
                            "§f/sfhelp - Display this help message\n" +
                    "§9---------------------------------------------------"
                ).send()
                1
            })
        }
    }

    fun sfcommandtest() {

        Command.register { dispatcher, _ ->
            dispatcher.register(literal("sfcommandtest").executes { context ->

                println("Executing /sfcommandtest for player: ${context.source.player}")

                ChatUtils.messageToChat("This is an info message example.")

                ChatUtils.warning("This is a warning message example. Be aware!")

                ChatUtils.error("This is an error message. Click me to copy this text.")

                ChatUtils.error(
                    errorMessage = "Another error. Click to copy '/skyfall help errors'.",
                    copyableText = "/skyfall help errors"
                )

                ChatUtils.messageToChat("All test messages have been sent successfully!")

                ChatUtils.messageToChat("Click me to copy!")
                    .copyContent("This is the text to copy")
                    .send()

                ChatUtils.messageToChat("Click me to run a command!")
                    .clickToRun("/help")
                    .send()

                1
            })
        }
    }
}