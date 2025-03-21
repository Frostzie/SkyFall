package io.github.frostzie.skyfall.commands

object CommandManager {
    fun loadCommands() {
        // Always On Commands
        AlwaysOnCommands.openConfig()
        AlwaysOnCommands.sfColor()
        AlwaysOnCommands.sfHelp()

        // Dungeon Commands
        DungeonCommands.requeue()
        DungeonCommands.floorCommands()
    }
}