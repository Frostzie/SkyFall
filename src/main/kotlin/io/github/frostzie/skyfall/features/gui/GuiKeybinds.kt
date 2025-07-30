package io.github.frostzie.skyfall.features.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.utils.KeyboardManager.isKeyClicked
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.lwjgl.glfw.GLFW

@Feature(name = "GUI Keybinds")
object GuiKeybinds : IEventFeature {

    override var isRunning = false

    private fun Int.isAssigned(): Boolean = this != GLFW.GLFW_KEY_UNKNOWN
    private val openConfigKey get() = SkyFall.feature.gui.openConfigKey
    private val openHudEditorKey get() = SkyFall.feature.gui.openHudEditorKey

    init {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (!isRunning || client.currentScreen != null) {
                return@register
            }

            if (openConfigKey.isAssigned() && openConfigKey.isKeyClicked()) {
                ConfigGuiManager.openConfigGui()
            }

            if (openHudEditorKey.isAssigned() && openHudEditorKey.isKeyClicked()) {
                ConfigGuiManager.openHudEditor()
            }
        }
    }

    override fun shouldLoad(): Boolean {
        return openConfigKey.isAssigned() || openHudEditorKey.isAssigned()
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
    }
}