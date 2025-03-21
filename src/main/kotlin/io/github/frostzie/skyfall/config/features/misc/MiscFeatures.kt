package io.github.frostzie.skyfall.config.features.misc

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiscFeatures {

    @Expose
    @ConfigOption(name = "Test Feature", desc = "Enables the /sftest cmd")
    @ConfigEditorBoolean
    var enabled: Boolean = false

}