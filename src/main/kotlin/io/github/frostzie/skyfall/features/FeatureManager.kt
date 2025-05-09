package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.dungeon.RequeueKey
import io.github.frostzie.skyfall.features.garden.GardenKeybinds
import io.github.frostzie.skyfall.features.gui.ConfigOpen
import io.github.frostzie.skyfall.features.misc.EntityHighlightFeature
import io.github.frostzie.skyfall.features.misc.ExampleFeature
import io.github.frostzie.skyfall.features.misc.Test.test
import io.github.frostzie.skyfall.features.misc.keybind.MiscKeybindManager

object FeatureManager {
    fun loadFeatures() {
        // GUI Features
        ConfigOpen()

        // Dungeon Features
        RequeueKey()

        // Misc Features
        MiscKeybindManager()

        // Garden Features
        GardenKeybinds.init()
        //MouseSensitivity

        // Dev Features

        // WIP features, might be removed or changed completely
        test()
        ExampleFeature.register()
        EntityHighlightFeature.register()
    }
}
