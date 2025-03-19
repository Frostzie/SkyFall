package io.github.frostzie.skyfall.config.features.dungeon

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class DungeonConfig {

    @Expose
    @ConfigOption(name = "Dungeon Test", desc = "Test if Dungeon Test is active")
    @ConfigEditorBoolean
    var DungeonTest: Boolean = false

    @Expose
    @ConfigOption(name = "Requeue Key", desc = "Press this key to requeue for another run")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var keyRequeue: Int = GLFW.GLFW_KEY_UNKNOWN
}