package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Misc {

    @Expose
    @ConfigOption(name = "Dead Rendering", desc = "Disables dead mob animation from being rendered.")
    @ConfigEditorBoolean
    var deadRendering: Boolean = false
}