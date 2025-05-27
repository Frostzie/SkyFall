package io.github.frostzie.skyfall.config.features.misc

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.misc.funny.FunnyConfig
import io.github.frostzie.skyfall.config.features.misc.keybind.KeyBinds
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MiscConfig {

    @Expose
    @Category(name = "Keybinds", desc = "")
    var keybinds = KeyBinds()

    @Expose
    @Category(name = "Funny", desc = "")
    var funny = FunnyConfig()

    @Expose
    @ConfigOption(name = "Hoppity Call", desc = "Alerts you when Hoppity is calling you.")
    @ConfigEditorBoolean
    var hoppityCallNotifier: Boolean = false

    @Expose
    @ConfigOption(name = "Hide Block Particles", desc = "Hide block breaking particles.")
    @ConfigEditorBoolean
    var blockBreakingParticles = false

    @Expose
    @ConfigOption(name = "Hide Potion Hud", desc = "Hide potion effect hud in inventory.")
    @ConfigEditorBoolean
    var hidePotionHud = false
}