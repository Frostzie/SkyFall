package io.github.frostzie.skyfall.features.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.gui.ConfigGuiManager
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object ConfigOpen {
    private var keyWasPressed = false

    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val openKey = SkyFall.feature.gui.openConfigKey
                val window = MinecraftClient.getInstance().window.handle

                if (openKey != GLFW.GLFW_KEY_UNKNOWN) {
                    val isPressed = if (openKey >= GLFW.GLFW_MOUSE_BUTTON_1 && openKey <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                        GLFW.glfwGetMouseButton(window, openKey) == GLFW.GLFW_PRESS
                    } else {
                        InputUtil.isKeyPressed(window, openKey)
                    }

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
            } else {
                keyWasPressed = false
            }
        }
    }
}