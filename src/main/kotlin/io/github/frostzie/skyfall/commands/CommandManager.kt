package io.github.frostzie.skyfall.commands

object CommandManager {
    fun loadCommands() {
        AlwaysOnCommands.openConfig()
        AlwaysOnCommands.sfColor()
        AlwaysOnCommands.sfHelp()
    }
}