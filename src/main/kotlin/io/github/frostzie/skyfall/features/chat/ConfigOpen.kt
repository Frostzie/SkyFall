package io.github.frostzie.skyfall.features.chat

import io.github.frostzie.skyfall.config.Features
import io.github.notenoughupdates.moulconfig.Config

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.minecraft.client.MinecraftClient
import java.io.File
/*
class ConfigOpen {
    init {
        ClientCommandRegistrationCallback.EVENT.register { a, b ->
            a.register(literal("sfconfig").executes {
                MinecraftClient.getInstance().send {
                    config.openConfigGui()
                }
                0
            })
        }
    }
}
*/