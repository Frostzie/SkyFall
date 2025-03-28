package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.dungeon.RequeueKey
import io.github.frostzie.skyfall.features.misc.ExampleFeature
import io.github.frostzie.skyfall.features.misc.Test.test
import io.github.frostzie.skyfall.features.misc.keybind.EquipmentMenuKeybind
import io.github.frostzie.skyfall.features.misc.keybind.PetsMenuKeybind
import io.github.frostzie.skyfall.features.misc.keybind.PotionBagKeybind
import io.github.frostzie.skyfall.features.misc.keybind.StorageMenuKeybind
import io.github.frostzie.skyfall.features.misc.keybind.TradeMenuKeybind
import io.github.frostzie.skyfall.features.misc.keybind.WardrobeMenuKeybind

object FeatureManager {
    fun loadFeatures() {
        // Dungeon Features
        RequeueKey()

        // Misc Features
        PetsMenuKeybind()
        WardrobeMenuKeybind()
        TradeMenuKeybind()
        PotionBagKeybind()
        EquipmentMenuKeybind()
        StorageMenuKeybind()

        // Test Features
        test()
        ExampleFeature.register()
    }
}