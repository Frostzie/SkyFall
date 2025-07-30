package io.github.frostzie.skyfall.config.features.gui

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GuiConfig {

    @Expose
    @ConfigOption(name = "Open Config Key", desc = "Keybind to open the config")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var openConfigKey: Int = GLFW.GLFW_KEY_UNKNOWN

    @ConfigOption(name = "Open Hud Editor", desc = "Press to Open Hud Editor")
    @ConfigEditorButton(buttonText = "Click!")
    var openHudEditorButton: Runnable = Runnable(ConfigGuiManager::openHudEditor)

    @Expose
    @ConfigOption(name = "Open Hud Editor", desc = "Keybind to open the Hud Editor")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var openHudEditorKey: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "More Hud Editor", desc = "Adds more Hud Editor customization options.")
    @ConfigEditorBoolean
    var fullHudEditor = false
}