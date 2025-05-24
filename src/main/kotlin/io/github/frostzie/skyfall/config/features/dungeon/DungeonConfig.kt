package io.github.frostzie.skyfall.config.features.dungeon

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class DungeonConfig {

    @Expose
    @ConfigOption(name = "Requeue Key", desc = "Press this key to requeue for another run")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var requeueKey: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Short Commands", desc = "Allow running /rq, /f1, /m1, etc. §cRequires game restart to take effect")
    @ConfigEditorBoolean
    var shortCommands: Boolean = false
}