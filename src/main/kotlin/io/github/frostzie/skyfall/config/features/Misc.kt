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
    @ConfigOption(name = "Pause Menu", desc = "")
    @Accordion
    val pauseMenu: PauseMenu = PauseMenu()
}

class PauseMenu {

    @Expose
    @ConfigOption(name = "Server Link Button", desc = "Hides server link button in the pause menu.")
    @ConfigEditorBoolean
    var hideServerLinkButton: Boolean = false
}