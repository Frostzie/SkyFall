package io.github.frostzie.skyfall

import io.github.frostzie.skyfall.commands.BaseCmd
import io.github.frostzie.skyfall.config.ConfigManager
import io.github.frostzie.skyfall.config.Features
import io.github.frostzie.skyfall.events.SlotRenderDispatch
import io.github.frostzie.skyfall.feature.mob.CustomHighlight
import io.github.frostzie.skyfall.feature.pets.ActivePet
import io.github.frostzie.skyfall.feature.pets.Autopet
import net.fabricmc.api.ClientModInitializer
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

        Autopet.load()
        BaseCmd.load()

        CustomHighlight.registerTick()

        SlotRenderDispatch.register(ActivePet)
    }

    companion object {
        lateinit var configManager: ConfigManager
        lateinit var features: Features

        var screenToOpen: Screen? = null
    }
}
