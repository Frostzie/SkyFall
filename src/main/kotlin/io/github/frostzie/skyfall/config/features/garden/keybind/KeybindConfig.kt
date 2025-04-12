package io.github.frostzie.skyfall.config.features.garden.keybind

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.features.garden.GardenKeybinds
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorButton
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorKeybind
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption
import io.github.notenoughupdates.moulconfig.observer.Property
import kotlinx.coroutines.Runnable
import org.lwjgl.glfw.GLFW

class KeybindConfig {

    @Expose
    @ConfigOption(name = "Enabled", desc = "Custom keybinds while in the Garden and holding a farming tool.")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    //TODO: when on, makes all config data not save
    //@Expose
    //@ConfigOption(name = "Reset All", desc = "Reset all keybinds to nothing")
    //@ConfigEditorButton(buttonText = "Reset")
    //var resetDefault = Runnable { GardenKeybinds.resetAll() }

    @Expose
    @ConfigOption(name = "Attack & Break", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_MOUSE_BUTTON_LEFT)
    var leftClick: Property<Int?> = Property.of<Int?>(GLFW.GLFW_MOUSE_BUTTON_LEFT)

    @Expose
    @ConfigOption(name = "Use Item", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_MOUSE_BUTTON_RIGHT)
    var rightClick: Property<Int?> = Property.of<Int?>(GLFW.GLFW_MOUSE_BUTTON_RIGHT)

    @Expose
    @ConfigOption(name = "Move Forwards", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_W)
    var moveForwards: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_W)

    @Expose
    @ConfigOption(name = "Move Left", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_A)
    var moveLeft: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_A)

    @Expose
    @ConfigOption(name = "Move Right", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_D)
    var moveRight: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_D)

    @Expose
    @ConfigOption(name = "Move Backwards", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_S)
    var moveBackwards: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_S)

    @Expose
    @ConfigOption(name = "Jump", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_SPACE)
    var moveJump: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_SPACE)

    @Expose
    @ConfigOption(name = "Sneak", desc = "")
    @ConfigEditorKeybind(defaultKey = GLFW.GLFW_KEY_LEFT_SHIFT)
    var moveSneak: Property<Int?> = Property.of<Int?>(GLFW.GLFW_KEY_LEFT_SHIFT)
}
