package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
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
    @ConfigOption(name = "Favorite Pets", desc = "")
    @Accordion
    val favoritePet: FavPet = FavPet()

    @Expose
    @ConfigOption(name = "Autopet", desc = "")
    @Accordion
    val autoPet: AutoPet = AutoPet()

}

class FavPet {

    @Expose
    @ConfigOption(name = "Enable", desc = "Enables highlighting for selected favorite pets.")
    @ConfigEditorBoolean
    var favEnable: Boolean = false

    @Expose
    @ConfigOption(name = "Favorite Color", desc = "Color of favorite pet highlight.")
    @ConfigEditorColour
    var favColor: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 255)

    @Expose
    @ConfigOption(name = "Favorite Key", desc = "Key pressed to favorite a pet.")
    @ConfigEditorKeybind(defaultKey = 0)
    var favKey: Int = 0

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
