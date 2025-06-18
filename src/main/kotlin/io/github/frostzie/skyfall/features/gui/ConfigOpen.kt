package io.github.frostzie.skyfall.features.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW

object ConfigOpen {
    fun init() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (client.currentScreen == null) {
                val openKey = SkyFall.feature.gui.openConfigKey

                if (openKey != GLFW.GLFW_KEY_UNKNOWN) {
                    if (openKey.isKeyClicked()) {
                        val player = client.player
                        if (player != null) {
                            ConfigGuiManager.openConfigGui()
                        }
                    }
                }
            }
        }
    }
}