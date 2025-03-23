package io.github.frostzie.skyfall.features.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.gui.ConfigGuiManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class ConfigOpen {
    private var keyWasPressed = false

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val openKey = SkyFall.Companion.feature.gui.openConfigKey
                if (openKey == -1) return@register
                val isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, openKey)

                if (isPressed && !keyWasPressed) {
                    val player = client.player
                    if (player != null) {
                        ConfigGuiManager.openConfigGui()
                    }
                }

                keyWasPressed = isPressed
            } else {
                keyWasPressed = false
            }
        }
    }
}