package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Pets {

    @Expose
    @ConfigOption(name = "Active Pet", desc = "Enables highlighting the currently active pet.")
    @ConfigEditorBoolean
    var activeEnabled: Boolean = false

    @Expose
    @ConfigOption(name = "Active Color", desc = "Color of the highlighting for the active pet")
    @ConfigEditorColour
    var activeColor: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 255)

    @Expose
    @ConfigOption(name = "Autopet", desc = "")
    @Accordion
    val autoPet: AutoPet = AutoPet()

}

class AutoPet {

    @Expose
    @ConfigOption(name = "Enable", desc = "Main toggle for the reset.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Combine Pet Level", desc = "Combines base level with the cosmetic level.")
    @ConfigEditorBoolean
    var combineLevel: Boolean = false

    @Expose
    @ConfigOption(name = "Shorten", desc = "Shortens the message.")
    @ConfigEditorBoolean
    var shorten: Boolean = false

}
