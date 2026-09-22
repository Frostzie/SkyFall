package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Misc {

    @Expose
    @ConfigOption(name = "Dead Rendering", desc = "Disables dead mob animation from being rendered.")
    @ConfigEditorBoolean
    var deadRendering: Boolean = false

    @Expose
    @ConfigOption(name = "Animations", desc = "")
    @Accordion
    val animations: Animations = Animations()

    @Expose
    @ConfigOption(name = "Pause Menu", desc = "")
    @Accordion
    val pauseMenu: PauseMenu = PauseMenu()
}

class Animations {

    @Expose
    @ConfigOption(name = "Disable Swing", desc = "Disables arm swinging animation.")
    @ConfigEditorBoolean
    var disableSwing: Boolean = false

    @Expose
    @ConfigOption(name = "Disable Re-Swing", desc = "Disables the same item being re-swung.\nHappens with items that have their item lore change in action. Due to Book of stats, accumulation enchants, etc.")
    @ConfigEditorBoolean
    var disableReSwing: Boolean = false

    @Expose
    @ConfigOption(name = "Instant Swap", desc = "Disables the **animation** when switching between items.")
    @ConfigEditorBoolean
    var instantItemSwap: Boolean = false
}

class PauseMenu {

    @Expose
    @ConfigOption(name = "Server Link Button", desc = "Hides server link button in the pause menu.")
    @ConfigEditorBoolean
    var hideServerLinkButton: Boolean = false
}