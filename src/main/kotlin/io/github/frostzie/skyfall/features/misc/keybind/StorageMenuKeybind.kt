package io.github.frostzie.skyfall.features.misc.keybind

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.SimpleTimeMark
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import kotlin.time.Duration.Companion.milliseconds

class StorageMenuKeybind {
    private var keyWasPressed = false
    private var lastCommandTime = SimpleTimeMark.farPast()

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val keybind = SkyFall.feature.miscFeatures.keybinds.storageMenuKeybind
                val window = MinecraftClient.getInstance().window.handle

                if (keybind != GLFW.GLFW_KEY_UNKNOWN) {
                    val isPressed = if (keybind >= GLFW.GLFW_MOUSE_BUTTON_1 && keybind <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                        GLFW.glfwGetMouseButton(window, keybind) == GLFW.GLFW_PRESS
                    } else {
                        InputUtil.isKeyPressed(window, keybind)
                    }

                    if (isPressed && !keyWasPressed && lastCommandTime.passedSince() >= 350.milliseconds) {
                        val player = client.player
                        if (player != null) {
                            player.networkHandler.sendChatCommand("storage")
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