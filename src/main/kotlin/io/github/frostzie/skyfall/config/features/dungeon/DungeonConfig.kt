package io.github.frostzie.skyfall.config.features.dungeon


import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DungeonConfig {

    @Expose
    @ConfigOption(name = "Dungeon Test", desc = "Test if Dungeon Test is active")
    @ConfigEditorBoolean
    var DungeonTest: Boolean = false

}