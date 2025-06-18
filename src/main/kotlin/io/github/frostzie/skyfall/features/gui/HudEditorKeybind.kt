package io.github.frostzie.skyfall.features.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.hud.HudEditorScreen
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW

object HudEditorKeybind {
    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val openKey = SkyFall.feature.gui.openHudEditorKey

                if (openKey != GLFW.GLFW_KEY_UNKNOWN) {
                    if (openKey.isKeyClicked()) {
                        client.setScreen(HudEditorScreen())
                    }
                }
            }
        }
    }
}