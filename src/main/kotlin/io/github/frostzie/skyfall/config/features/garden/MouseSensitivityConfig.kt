package io.github.frostzie.skyfall.config.features.garden

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class MouseSensitivityConfig() {

    @Expose
    @ConfigOption(name = "Mouse Sensitivity", desc = "Change the mouse sensitivity")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var mouseSensitivity = GLFW.GLFW_KEY_UNKNOWN
}