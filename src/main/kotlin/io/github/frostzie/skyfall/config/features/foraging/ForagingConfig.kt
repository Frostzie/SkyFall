package io.github.frostzie.skyfall.config.features.foraging

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ForagingConfig {

    @Expose
    @ConfigOption(name = "", desc = "")
    @ConfigEditorInfoText(infoTitle = "§fMiddle Click the perk to add it to the HUD!")
    var infoText: String = ""

    @Expose
    @ConfigOption(name = "Whisper HUD", desc = "Shows amount of whispers needed to level up a HOTF perk.\n§eMake sure whisper widget is enabled in tab!")
    @ConfigEditorBoolean
    var neededWhisperHud: Boolean = true

    @Expose
    @ConfigOption(name = "One Perk at a Time", desc = "Shows the whispers you need for 1 perk at a time.\nThis doesn't mean only 1 perk can be Selected!")
    @ConfigEditorBoolean
    var onePerkAtATime: Boolean = true
}