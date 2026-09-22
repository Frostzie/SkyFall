package io.github.frostzie.skyfall.commands

import com.github.stivais.commodore.Commodore
import io.github.frostzie.skyfall.config.ConfigGuiManager

val mainCommands = Commodore("skyfall", "sf") {
    runs { search: String? ->
        ConfigGuiManager.openConfigGui(search)
    }
}