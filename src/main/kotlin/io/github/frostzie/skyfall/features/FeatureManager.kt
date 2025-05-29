package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.chat.FilterManager
import io.github.frostzie.skyfall.features.dungeon.RequeueKey
import io.github.frostzie.skyfall.features.garden.GardenKeybinds
import io.github.frostzie.skyfall.features.gui.ConfigOpen
import io.github.frostzie.skyfall.features.misc.funny.RealisticSpacemanHelmet
import io.github.frostzie.skyfall.features.misc.keybind.MiscKeybindManager
import io.github.frostzie.skyfall.features.garden.MouseSensitivity
import io.github.frostzie.skyfall.features.inventory.FavoriteAbiContact
import io.github.frostzie.skyfall.features.inventory.FavoritePowerStone
import io.github.frostzie.skyfall.hud.HudEditorKeybind

object FeatureManager {
    fun loadFeatures() {
        // GUI Features
        ConfigOpen.init()
        HudEditorKeybind.init()

        // Chat Features
        FilterManager

        // Dungeon Features
        RequeueKey()

        // IInventory Features
        FavoritePowerStone.init()
        FavoriteAbiContact.init()

        // Misc Features
        MiscKeybindManager()
        RealisticSpacemanHelmet.init()

        // Garden Features
        GardenKeybinds.init()
        GardenKeybinds.homeHotkey()
        MouseSensitivity.init()
    }
}
