package io.github.frostzie.skyfall.config.features.inventory

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class InventoryConfig {
    //TODO: add a way to change the color of the highlight and make the green glass not be rendered in favorite power stone
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
        //TODO: soon™
        //@Expose
        //@ConfigOption(name = "Highlight Active", desc = "Highlights the currently active pet")
        //@ConfigEditorBoolean
        //var activePet: Boolean = false

        @Expose
        @ConfigOption(name = "Favorite Hotkey", desc = "Pressing the keybind over a pet will highlight it. Adds a toggle item in the top right corner to only show favorites.")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN
    }


}