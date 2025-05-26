package io.github.frostzie.skyfall.hud

import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object HudEditorKeybind {
    private var keyWasPressed = false

    fun initialize() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val openKey = SkyFall.feature.gui.openHudEditorKey // Use the shared GuiConfig instance
                val window = MinecraftClient.getInstance().window.handle

                if (openKey != GLFW.GLFW_KEY_UNKNOWN) {
                    val isPressed = if (openKey >= GLFW.GLFW_MOUSE_BUTTON_1 && openKey <= GLFW.GLFW_MOUSE_BUTTON_LAST) {
                        GLFW.glfwGetMouseButton(window, openKey) == GLFW.GLFW_PRESS
                    } else {
                        InputUtil.isKeyPressed(window, openKey)
                    }

                    if (isPressed && !keyWasPressed) {
                        client.setScreen(HudEditorScreen())
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