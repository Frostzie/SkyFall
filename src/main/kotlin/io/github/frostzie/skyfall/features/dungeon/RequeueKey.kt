package io.github.frostzie.skyfall.features.dungeon

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import kotlin.time.Duration.Companion.seconds

class RequeueKey {
    private var keyWasPressed = false

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val requeueKey = SkyFall.feature.dungeon.requeueKey
                val isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().window.handle, requeueKey)

                if (isPressed && !keyWasPressed) {
                    val player = client.player
                    if (player != null) {
                        player.networkHandler.sendChatCommand("instancerequeue")
                        ChatUtils.messageToChat("§aRequeueing")
                    }
                }

                keyWasPressed = isPressed
            } else {
                keyWasPressed = false
            }
        }
    }
}