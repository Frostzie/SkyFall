package io.github.frostzie.skyfall.config.features.misc.funny

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class FunnyConfig {

    @Expose
    @Accordion
    @ConfigOption(name = "Spaceman Helmet", desc = "")
    var spacemanHelmetConfig = SpacemanHelmetConfig()

    class SpacemanHelmetConfig {
        @Expose
        @ConfigOption(name = "Spaceman Helmet", desc = "Makes the Spaceman Helmet glass render in first person")
        @ConfigEditorBoolean
        var realisticSpacemanHelmet: Boolean = false

        @Expose
        @ConfigOption(name = "Only if Equipped", desc = "Only render the Spaceman Helmet if wearing it")
        @ConfigEditorBoolean
        var onlyIfEquipped: Boolean = true
    }
}