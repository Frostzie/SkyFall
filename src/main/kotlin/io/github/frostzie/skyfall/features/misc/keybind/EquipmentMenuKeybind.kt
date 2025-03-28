package io.github.frostzie.skyfall.features.misc.keybind

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil

class EquipmentMenuKeybind {
    private var keyWasPressed = false

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val keybind = SkyFall.feature.miscFeatures.keybinds.equipmentMenuKeybind
                val isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, keybind)

                if (isPressed && !keyWasPressed) {
                    val player = client.player
                    if (player != null) {
                        player.networkHandler.sendChatCommand("equipment")
                    }
                }

                keyWasPressed = isPressed
            } else {
                keyWasPressed = false
            }
        }
    }
}
