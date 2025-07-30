package io.github.frostzie.skyfall.config.features.mining

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiningConfig {

    @Expose
    @ConfigOption(name = "Info", desc = "§fMiddle Click the perk to add it to the HUD!")
    @ConfigEditorInfoText(infoTitle = "")
    var infoText: String = ""

    @Expose
    @ConfigOption(name = "Powder HUD", desc = "Shows amount of power needed to level up a HOTM perk.")
    @ConfigEditorBoolean
    var neededPowderHud: Boolean = true

    @Expose
    @ConfigOption(name = "One Perk at a Time", desc = "Shows the powder you need for 1 perk at a time.\nThis doesn't mean only 1 perk can be Selected!")
    @ConfigEditorBoolean
    var onePerkAtATime: Boolean = true
}