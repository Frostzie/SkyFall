package io.github.frostzie.skyfall.config.features.inventory

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class InventoryConfig {
    //TODO: add a way to change the color of the highlight
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
        @ConfigOption(name = "Show Max Stat Boost", desc = "Shows the maximum stat boost under each attribute.")
        @ConfigEditorBoolean
        var showMaxStatBoost: Boolean = true
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
    }
}