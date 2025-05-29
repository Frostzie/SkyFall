package io.github.frostzie.skyfall.commands

import io.github.frostzie.skyfall.features.dungeon.ShortCommands

object CommandManager {
    fun loadCommands() {
        // Always On Commands
        AlwaysOnCommands.openConfig()
        AlwaysOnCommands.sfColor()

        // Dungeon Commands
        ShortCommands.registerCommands()
    }
}