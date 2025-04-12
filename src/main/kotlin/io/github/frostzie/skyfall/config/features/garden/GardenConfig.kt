package io.github.frostzie.skyfall.config.features.garden

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.garden.keybind.KeybindConfig
import io.github.frostzie.skyfall.config.features.misc.keybind.KeyBinds
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
    @ConfigOption(name = "Mouse Sensitivity", desc = "Change the mouse sensitivity")
    var mouseSensitivity = MouseSensitivityConfig()

    @Expose
    @ConfigOption(name = "Garden Check", desc = "Check if in garden") //TODO: Move this to dev category and make check what island
    @ConfigEditorBoolean
    var GardenTest: Boolean = false

}

