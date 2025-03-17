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
    @ConfigOption(name = "Mouse Sensitivity", desc = "Change the mouse sensitivity")
    var mouseSensitivity = MouseSensitivityConfig()

    @Expose
    @ConfigOption(name = "Garden Test", desc = "Test if Garden Test is active")
    @ConfigEditorBoolean
    var GardenTest: Boolean = false

}

