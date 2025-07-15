package io.github.frostzie.skyfall.config.features.inventory

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class InventoryConfig {
    //TODO: remove green glass pane in Power Stone menu
    @Expose
    @Accordion
    @ConfigOption(name = "Attribute Menu", desc = "")
    var attributeMenu: AttributeMenuConfig = AttributeMenuConfig()

    class AttributeMenuConfig {
        @Expose
        @ConfigOption(name = "Highlight Disabled", desc = "Highlights attributes that are disabled in red.")
        @ConfigEditorBoolean
        var highlightDisabled: Boolean = true

        @Expose
        @ConfigOption(name = "Highlight Maxed", desc = "Highlights attributes that are maxed in Gold.")
        @ConfigEditorBoolean
        var highlightMaxed: Boolean = true

        @Expose
        @ConfigOption(name = "Show Max Stat Boost", desc = "Shows the maximum stat boost under each attribute.")
        @ConfigEditorDropdown
        var showMaxStatBoost: ShowMaxBoost = ShowMaxBoost.SHIFT

        enum class ShowMaxBoost(private val value: String) {
            ALWAYS("Always"),
            SHIFT("Shift"),
            CONTROL("Control"),
            ALT("Alt"),
            NEVER("Never");

            override fun toString(): String = value
        }

        @Expose
        @ConfigOption(name = "Shard left to Max", desc = "Shows the amount of shards left to max the attribute.")
        @ConfigEditorDropdown
        var showShardsLeftToMax: LeftToMax = LeftToMax.ALWAYS

        enum class LeftToMax(val value: String) {
            ALWAYS("Always"),
            SHIFT("Shift"),
            CONTROL("Control"),
            ALT("Alt"),
            NEVER("Never");

            override fun toString(): String = value
        }

        @Expose
        @ConfigOption(name = "How to Obtain+", desc = "Shows extra info on how to obtain the attribute.")
        @ConfigEditorDropdown
        var obtainOption: ObtainShow = ObtainShow.SHIFT

        enum class ObtainShow(val value: String) {
            ALWAYS("Always"),
            SHIFT("Shift"),
            CONTROL("Control"),
            ALT("Alt"),
            NEVER("Never");

            override fun toString(): String = value
        }

        @Expose
        @ConfigOption(name = "Level Number", desc = "Show what level the attribute is with a stack number.")
        @ConfigEditorBoolean
        var levelNumber: Boolean = true

        @Expose
        @ConfigOption(name = "Hide description", desc = "Hide the gray description inside Hunting Box inventory")
        @ConfigEditorBoolean
        var hideDescription: Boolean = false
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Favorite Power Stone", desc = "")
    var powerStone: PowerStoneHighlight = PowerStoneHighlight()

    class PowerStoneHighlight {
        @Expose
        @ConfigOption(name = "Hotkey", desc = "Pressing the keybind over the power stone will highlight it. Adds a toggle item in the top right corner to only show favorites.")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "Color", desc = "The color of the favorite power stone.")
        @ConfigEditorColour
        var powerStoneColor: String = "0:220:255:170:0"
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Favorite Abi Contact", desc = "")
    var abiContact: FavoriteAbiContact = FavoriteAbiContact()

    class FavoriteAbiContact {
        @Expose
        @ConfigOption(name = "Hotkey", desc = "Pressing the keybind over a contact will highlight it. Adds a toggle item in the top right corner to only show favorites.")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "Color", desc = "The color of the favorite abi contact.")
        @ConfigEditorColour
        var abiContactColor: String = "0:220:255:170:0"
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Pet Menu", desc = "")
    var petMenu: PetMenuConfig = PetMenuConfig()

    class PetMenuConfig {
        @Expose
        @ConfigOption(name = "Highlight Active", desc = "Highlights the currently active pet")
        @ConfigEditorBoolean
        var activePet: Boolean = false

        @Expose
        @ConfigOption(name = "Favorite Hotkey", desc = "Pressing the keybind over a pet will highlight it. Adds a toggle item in the top right corner to only show favorites.")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN

        @Expose
        @ConfigOption(name = "Color", desc = "The color of the favorite pets.")
        @ConfigEditorColour
        var petHighlightColor: String = "0:220:255:170:0"

        @Expose
        @ConfigOption(name = "Color", desc = "The color of the active pet.")
        @ConfigEditorColour
        var petActiveColor: String = "0:220:0:255:0" //0, 255, 0, 220
    }
}