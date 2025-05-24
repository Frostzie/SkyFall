package io.github.frostzie.skyfall.config.features.garden

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.garden.keybind.KeybindConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GardenConfig {

    @Expose
    @Category(name = "Keybinds", desc = "")
    var keybindConfig = KeybindConfig()

    @Expose
    @Accordion
    @ConfigOption(name = "Mouse Sensitivity", desc = "")
    var mouseSensitivity: MouseSensitivityConfig = MouseSensitivityConfig()

    class MouseSensitivityConfig {
        @Expose
        @ConfigOption(name = "Mouse Sensitivity", desc = "Change the mouse sensitivity")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var mouseSensitivity = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "On Garden", desc = "Only works on the garden island")
        @ConfigEditorBoolean
        var onGarden = true
    }
}