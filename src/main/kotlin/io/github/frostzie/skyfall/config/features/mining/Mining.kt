package io.github.frostzie.skyfall.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property

class Mining {

    @Expose
    @ConfigOption(name = "Shaft", desc = "")
    @Accordion
    val shaft: Shaft = Shaft()

}

class Shaft {

    @Expose
    @ConfigOption(name = "Party Announcer", desc = "Sends chat message in party chat after entering a mineshaft.")
    @ConfigEditorBoolean
    var partyAnnouncer: Boolean = false

}