package io.github.frostzie.skyfall.features.misc.keybind

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class PotionBagKeybind {
    private var keyWasPressed = false

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val keybind = SkyFall.feature.miscFeatures.keybinds.potionBagKeybind
                val isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, keybind)

                if (isPressed && !keyWasPressed) {
                    val player = client.player
                    if (player != null) {
                        player.networkHandler.sendChatCommand("potbag")
                    }
                }

                keyWasPressed = isPressed
            } else {
                keyWasPressed = false
            }
        }
    }
}
