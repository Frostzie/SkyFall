package io.github.frostzie.skyfall.config.features.misc

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.misc.funny.FunnyConfig
import io.github.frostzie.skyfall.config.features.misc.keybind.KeyBinds
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiscConfig {

    @Expose
    @Category(name = "Keybinds", desc = "")
    var keybinds = KeyBinds()

    @Expose
    @Category(name = "Funny", desc = "")
    var funny = FunnyConfig()

    @Expose
    @ConfigOption(name = "Test Feature", desc = "Enables the /sftest cmd")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Render Triangle", desc = "Enable rendering of a triangle on the screen")
    @ConfigEditorBoolean
    var renderTriangle: Boolean = false
}