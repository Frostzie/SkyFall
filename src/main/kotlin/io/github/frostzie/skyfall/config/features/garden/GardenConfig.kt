package io.github.frostzie.skyfall.config.features.garden

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GardenConfig {
    @Expose
    @Accordion
    @ConfigOption(name = "Mouse Sensitivity", desc = "")
    var mouseSensitivity: MouseSensitivityConfig = MouseSensitivityConfig()

    class MouseSensitivityConfig {
        @Expose
        @ConfigOption(name = "Disable Mouse", desc = "Disable the mouse sensitivity hotkey")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var mouseSensitivity = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "On Garden", desc = "Only works on the garden island")
        @ConfigEditorBoolean
        var onGarden = true
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Garden Map", desc = "")
    var gardenMap: GardenMapConfig = GardenMapConfig()

    class GardenMapConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the garden map feature")
        @ConfigEditorBoolean
        var enabled = true
    }
}