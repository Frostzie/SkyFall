package io.github.frostzie.skyfall

import io.github.frostzie.skyfall.commands.mainCommands
import io.github.frostzie.skyfall.config.ConfigManager
import io.github.frostzie.skyfall.config.Features
import io.github.frostzie.skyfall.events.SlotRenderDispatch
import io.github.frostzie.skyfall.events.SlotClickDispatch
import io.github.frostzie.skyfall.feature.mob.CustomHighlight
import io.github.frostzie.skyfall.feature.pets.ActivePet
import io.github.frostzie.skyfall.feature.pets.Autopet
import io.github.frostzie.skyfall.feature.pets.FavoritePets
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screens.Screen

class SkyFall : ClientModInitializer {
    override fun onInitializeClient() {
        configManager = ConfigManager()
        features = configManager.config!!

        ClientLifecycleEvents.CLIENT_STOPPING.register {
            configManager.save()
        }

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            if (client.player == null) return@EndTick

            if (screenToOpen != null) {
                client.gui.setScreen(screenToOpen)
                screenToOpen = null
            }
        })

        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            arrayOf(
                mainCommands,
                CustomHighlight.highlightCommands
            ).forEach { commodore -> commodore.register(dispatcher) }
        }

        Autopet.load()

        CustomHighlight.registerTick()

        SlotRenderDispatch.register(FavoritePets)
        SlotRenderDispatch.register(ActivePet) // Needs to be below fav so it highlights over it
        SlotClickDispatch.register(FavoritePets)
    }

    companion object {
        lateinit var configManager: ConfigManager
        lateinit var features: Features

        var screenToOpen: Screen? = null
    }
}
