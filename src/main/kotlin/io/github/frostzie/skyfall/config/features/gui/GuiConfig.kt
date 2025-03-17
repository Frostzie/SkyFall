package io.github.frostzie.skyfall.config.features.gui

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GuiConfig {

    @Expose
    @ConfigOption(name = "Open Menu Key", desc = "Set a keybind to open the config menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var keyOpenConfig: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Says msg in chat", desc = "yes")
    @ConfigEditorBoolean
    var saysMsg: Boolean = false
}