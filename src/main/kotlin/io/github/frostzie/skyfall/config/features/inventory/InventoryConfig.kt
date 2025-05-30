package io.github.frostzie.skyfall.config.features.inventory

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

class InventoryConfig {
    //TODO: add a way to change the color of the highlight
    @Expose
    @Accordion
    @ConfigOption(name = "Favorite Power Stone", desc = "")
    var powerStone: PowerStoneHighlight = PowerStoneHighlight()

    class PowerStoneHighlight {
        @Expose
        @ConfigOption(name = "Hotkey", desc = "Pressing the keybind over the power stone will highlight it")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN
    }

    @Expose
    @Accordion
    @ConfigOption(name = "Favorite Abi Contact", desc = "")
    var abiContact: FavoriteAbiContact = FavoriteAbiContact()

    class FavoriteAbiContact {
        @Expose
        @ConfigOption(name = "Hotkey", desc = "Pressing the keybind over a contact will highlight it")
        @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
        var favoriteKey: Int = GLFW.GLFW_KEY_UNKNOWN
    }
}