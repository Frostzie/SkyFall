package io.github.frostzie.skyfall.config.features.garden

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class GardenConfig {
    @Expose
    @Accordion
    @ConfigOption(name = "Mouse Sensitivity", desc = "")
    var mouseSensitivity: MouseSensitivityConfig = MouseSensitivityConfig()

    class MouseSensitivityConfig {
        @Expose
        @ConfigOption(name = "Disable Mouse", desc = "Disable the mouse sensitivity hotkey")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var mouseSensitivity = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "On Garden", desc = "Only works on the garden island")
        @ConfigEditorBoolean
        var onGarden = true
    }

    @Expose
    @ConfigOption(name = "Spawn Keybind", desc = "Keybind to teleport to the garden spawn")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var warpToSpawn = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @Accordion
    @ConfigOption(name = "Garden Map", desc = "")
    var gardenMap: GardenMapConfig = GardenMapConfig()

    class GardenMapConfig {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the garden map feature")
        @ConfigEditorBoolean
        var enabled = true

        @Expose
        @ConfigOption(name = "Notice", desc = "Ability to change colors for map elements will be in the next beta release!")
        @ConfigEditorInfoText
        var infoText: String = ""

        @Expose
        @ConfigOption(name = "Background Color", desc = "The background color.")
        @ConfigEditorColour
        var backgroundColor: String = "0:250:13:13:13"

        @Expose
        @ConfigOption(name = "Plot Color", desc = "The default color of plots.")
        @ConfigEditorColour
        var defaultPlotColor: String = "0:255:211:211:211"

        //TODO: add locked plot detection
        //@Expose
        //@ConfigOption(name = "Locked Plot Color", desc = "The color of the plot if it hasn't been unlocked.")
        //@ConfigEditorColour
        //var lockedPlotColor: String = "0:70:70:70:255"

        @Expose
        @ConfigOption(name = "Text Color", desc = "Visitor and total pest text color.")
        @ConfigEditorColour
        var textColor: String = "0:255:255:255:255"

        @Expose
        @ConfigOption(name = "Plot Text", desc = "Plot Number color.")
        @ConfigEditorColour
        var plotTextColor: String = "0:255:0:0:0"

        @Expose
        @ConfigOption(name = "Pest Plot Color", desc = "The color of the plot if it has a pest.")
        @ConfigEditorColour
        var pestColor: String = "0:255:255:179:0"

        @Expose
        @ConfigOption(name = "Sprayed Plot Color", desc = "The color of the plot if it has been sprayed.")
        @ConfigEditorColour
        var sprayColor: String = "0:255:0:249:255"


        @Expose
        @ConfigOption(name = "Player Icon", desc = "The type of icon that should be used for the player.")
        @ConfigEditorDropdown
        var playerIconType: PlayerIcon = PlayerIcon.ARROW

        enum class PlayerIcon(private val value: String) {
            ARROW("Map Arrow"),
            CUBE("Cube / Dot");

            override fun toString(): String = value
        }

        @Expose
        @ConfigOption(name = "Player Icon Color", desc = "The color of the player icon if the cube is chosen.")
        @ConfigEditorColour
        var playerIconColor: String = "0:150:255:0:112"
    }
}