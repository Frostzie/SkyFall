package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MobHighlight {

    @Expose
    @ConfigOption(name = "Enable", desc = "Main toggle to enable custom mob highlighting.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Color", desc = "Color to highlight the mobs.")
    @ConfigEditorColour
    var color: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 255)
}