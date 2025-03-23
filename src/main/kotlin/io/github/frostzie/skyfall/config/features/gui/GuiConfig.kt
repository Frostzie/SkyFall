package io.github.frostzie.skyfall.config.features.gui

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GuiConfig {

    @Expose
    @ConfigOption(name = "Open Config Key", desc = "Keybind to open the config")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var openConfigKey: Int = GLFW.GLFW_KEY_UNKNOWN
}