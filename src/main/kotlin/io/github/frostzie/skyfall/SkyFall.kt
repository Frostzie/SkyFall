package io.github.frostzie.skyfall

import io.github.frostzie.skyfall.commands.mainCommands
import io.github.frostzie.skyfall.config.ConfigManager
import io.github.frostzie.skyfall.config.Features
import io.github.frostzie.skyfall.feature.mob.CustomHighlight
import io.github.frostzie.skyfall.feature.pets.ActivePet
import io.github.frostzie.skyfall.feature.pets.Autopet
import io.github.frostzie.skyfall.feature.pets.FavoritePets
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.gui.screens.Screen
import tech.thatgravyboat.skyblockapi.api.SkyBlockAPI

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

        SkyBlockAPI.eventBus.register(this)

        Autopet.load()

        CustomHighlight.registerTick()

        ActivePet.register()
        FavoritePets.register()
    }

    companion object {
        lateinit var configManager: ConfigManager
        lateinit var features: Features

        var screenToOpen: Screen? = null
    }
}
