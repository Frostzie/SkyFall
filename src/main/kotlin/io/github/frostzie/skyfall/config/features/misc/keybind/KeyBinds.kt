package io.github.frostzie.skyfall.config.features.misc.keybind

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import org.lwjgl.glfw.GLFW

//TODO: Remove this and either recommend skytils or add new overall system for hotkeys

class KeyBinds {

    @Expose
    @ConfigOption(name = "Pets", desc = "Keybind to open the pets menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var petsMenuKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Storage", desc = "Keybind to open the storage menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var storageMenuKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Wardrobe", desc = "Keybind to open the wardrobe menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var wardrobeMenuKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Equipment", desc = "Keybind to open the equipment menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var equipmentMenuKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Potion Bag", desc = "Keybind to open the potion bag")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var potionBagKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

    @Expose
    @ConfigOption(name = "Trade Menu", desc = "Keybind to open the trade menu")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_UNKNOWN)
    var tradeMenuKeybind: Int = GLFW.GLFW_KEY_UNKNOWN

}