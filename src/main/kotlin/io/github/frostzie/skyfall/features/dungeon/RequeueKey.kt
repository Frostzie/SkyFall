package io.github.frostzie.skyfall.features.dungeon

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

class RequeueKey {
    private var keyWasPressed = false
    private var lastCommandTime = SimpleTimeMark.farPast()

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val requeueKey = SkyFall.feature.dungeon.requeueKey
                val window = MinecraftClient.getInstance().window.handle

                if (requeueKey != GLFW.GLFW_KEY_UNKNOWN) {
                    val isPressed = if (requeueKey >= GLFW.GLFW_MOUSE_BUTTON_1 && requeueKey <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                        GLFW.glfwGetMouseButton(window, requeueKey) == GLFW.GLFW_PRESS
                    } else {
                        InputUtil.isKeyPressed(window, requeueKey)
                    }

                    if (isPressed && !keyWasPressed && lastCommandTime.passedSince() >= 350.milliseconds) {
                        val player = client.player
                        if (player != null) {
                            player.networkHandler.sendChatCommand("instancerequeue")
                            ChatUtils.messageToChat("§aRequeueing").send()
                            lastCommandTime = SimpleTimeMark.now()
                        }
                    }
                    keyWasPressed = isPressed
                } else {
                    keyWasPressed = false
                }
            } else {
                keyWasPressed = false
            }
        }
    }
}