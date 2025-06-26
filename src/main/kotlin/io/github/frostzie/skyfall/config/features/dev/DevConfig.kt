package io.github.frostzie.skyfall.config.features.dev

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.config.features.garden.keybind.KeybindConfig
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class DevConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable dev features")
    @ConfigEditorBoolean
    var enabledDevMode: Boolean = false

    @Expose
    @ConfigOption(name = "Location Command", desc = "Enable the /sfDevIslandType command to show your current island name and area.")
    @ConfigEditorBoolean
    var locationCommand: Boolean = false

    @Expose
    @ConfigOption(name = "Sound Detector", desc = "Enable the sound detector feature to see all sounds played in the game.")
    @ConfigEditorBoolean
    var soundDetector: Boolean = false

    @Expose
    @ConfigOption(name = "Copy Item Data", desc = "Howerover an item in inventory and press the keybind to copy it's data to clipboard.")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var copyItemDataKey: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @Category(name = "Repo Builder", desc = "")
    var repo = Repo()

    class Repo {
        @Expose
        @ConfigOption(name = "", desc = "§cOnly use if you know what your are doing!")
        @ConfigEditorInfoText
        var infoText: String = ""

        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the Attribute Menu repo builder feature")
        @ConfigEditorBoolean
        var attributeMenuRepoBuilder: Boolean = false

        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the Attribute Menu individual repo builder feature")
        @ConfigEditorBoolean
        var attributeMenuInfoRepoBuilder: Boolean = false

        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the Attribute data builder for bazaar info.")
        @ConfigEditorBoolean
        var attributeDataFromBazaar: Boolean = false
    }

}