package io.github.frostzie.skyfall.commands

import io.github.frostzie.skyfall.features.dungeon.ShortCommands

object CommandManager {
    fun loadCommands() {
        // Always On Commands
        AlwaysOnCommands.openConfig()
        AlwaysOnCommands.sfColor()
        AlwaysOnCommands.sfHelp()
        AlwaysOnCommands.sfcommandtest()

        // Dungeon Commands
        ShortCommands.registerCommands()
    }
}