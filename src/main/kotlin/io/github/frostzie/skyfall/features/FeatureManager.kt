package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.dungeon.RequeueKey
import io.github.frostzie.skyfall.features.garden.GardenKeybinds
import io.github.frostzie.skyfall.features.gui.ConfigOpen
import io.github.frostzie.skyfall.features.misc.funny.RealisticSpacemanHelmet
import io.github.frostzie.skyfall.features.misc.keybind.MiscKeybindManager
import io.github.frostzie.skyfall.features.garden.MouseSensitivity
import io.github.frostzie.skyfall.features.wip.test


object FeatureManager {
    fun loadFeatures() {
        // GUI Features
        ConfigOpen()

        // Dungeon Features
        RequeueKey()

        // Misc Features
        MiscKeybindManager()
        RealisticSpacemanHelmet.init()

        // Garden Features
        GardenKeybinds.init()
        GardenKeybinds.homeHotkey()
        MouseSensitivity.init()

        // Dev Features

        // WIP features. Might be removed or changed completely
        //IslandCommand.register()
        //test.initialize()
        //ExampleFeature.register()
        //ItemDetectorDemo.register()
    }
}
