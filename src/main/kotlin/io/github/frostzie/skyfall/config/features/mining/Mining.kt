package io.github.frostzie.skyfall.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Mining {

    @Expose
    @ConfigOption(name = "Shaft", desc = "")
    @Accordion
    val shaft: Shaft = Shaft()

}

class Shaft {

    @Expose
    @ConfigOption(name = "Party Announcer", desc = "Sends chat message in party chat after entering a mineshaft.\nContains Shaft type and corpses.")
    @ConfigEditorBoolean
    var partyAnnouncer: Boolean = false

}