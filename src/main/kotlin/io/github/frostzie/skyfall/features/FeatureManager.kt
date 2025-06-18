package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.foraging.TuneFrequency
import io.github.frostzie.skyfall.features.chat.FilterManager
import io.github.frostzie.skyfall.features.dev.SoundDetector
import io.github.frostzie.skyfall.features.dungeon.RequeueKey
import io.github.frostzie.skyfall.features.garden.GardenKeybinds
import io.github.frostzie.skyfall.features.gui.ConfigOpen
import io.github.frostzie.skyfall.features.misc.funny.RealisticSpacemanHelmet
import io.github.frostzie.skyfall.features.misc.keybind.MiscKeybindManager
import io.github.frostzie.skyfall.features.garden.MouseSensitivity
import io.github.frostzie.skyfall.features.inventory.FavoriteAbiContact
import io.github.frostzie.skyfall.features.inventory.FavoritePet
import io.github.frostzie.skyfall.features.inventory.FavoritePowerStone
import io.github.frostzie.skyfall.features.gui.HudEditorKeybind

object FeatureManager {
    fun loadFeatures() {
        // GUI Features
        ConfigOpen.init()
        HudEditorKeybind.init()

        // Chat Features
        FilterManager.loadFilters()

        // Dungeon Features
        RequeueKey.init()

        // Inventory Features
        FavoritePowerStone.init()
        FavoriteAbiContact.init()
        FavoritePet.init()

        // Misc Features
        MiscKeybindManager.init()
        RealisticSpacemanHelmet.init()

        // Garden Features
        GardenKeybinds.init()
        GardenKeybinds.homeHotkey()
        MouseSensitivity.init()

        // Foraging Features
        TuneFrequency.init() //TODO: rework

        // Dev Features
        SoundDetector.init() //TODO: Fix hud flickering after fading out
        //CopyItemData.init()

        //WIP Features
        //WaypointFeature.init()
        //BoxFeature.init()
        //ExampleElements.initialize()
        //Test.init() //Garden Map

    }
}
