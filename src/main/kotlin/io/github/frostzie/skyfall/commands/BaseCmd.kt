package io.github.frostzie.skyfall.commands

import com.mojang.brigadier.arguments.StringArgumentType
import io.github.frostzie.skyfall.config.ConfigGuiManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.command.v2.ClientCommands

object BaseCmd {
    fun load() {
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            dispatcher.register(
                ClientCommands.literal("skyfall")
                    .executes { ConfigGuiManager.openConfigGui(null); 1 }
                    //There probably might be a better way of doing this, but I don't know and this works lol
                    //TODO: need to finish reading the docs that might help...
                    .then(ClientCommands.argument("search", StringArgumentType.string())
                        .executes {
                            val search = StringArgumentType.getString(it, "search")
                            ConfigGuiManager.openConfigGui(search.ifBlank { null })
                            1
                        }
                    ))
        }
    }
}