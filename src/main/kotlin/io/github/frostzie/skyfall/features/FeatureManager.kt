package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.features.chat.FilterManager
import io.github.frostzie.skyfall.features.dev.repo.AttributeMenuInfoRepoBuilder
import io.github.frostzie.skyfall.features.dev.repo.AttributeMenuRepoBuilder
import io.github.frostzie.skyfall.features.dev.ItemStackJsonCopier
import io.github.frostzie.skyfall.features.dev.SoundDetector
import io.github.frostzie.skyfall.features.dev.repo.AttributeDataFromBazaar
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
import io.github.frostzie.skyfall.features.inventory.attribute.AttributeMenu
import io.github.frostzie.skyfall.features.inventory.attribute.HideDescription
import io.github.frostzie.skyfall.features.inventory.attribute.LevelNumber
import io.github.frostzie.skyfall.features.inventory.attribute.ShowInBazaar

//TODO: rework whole loading system so not all features are loaded on startup
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
        AttributeMenu.init()
        HideDescription.init()
        ShowInBazaar.init()
        LevelNumber.init()

        // Misc Features
        MiscKeybindManager.init()
        RealisticSpacemanHelmet.init()

        // Garden Features
        GardenKeybinds.init()
        GardenKeybinds.homeHotkey()
        MouseSensitivity.init()

        // Dev Features
        SoundDetector.init() //TODO: Fix hud flickering after fading out
        ItemStackJsonCopier.init()
        AttributeMenuRepoBuilder.init()
        AttributeMenuInfoRepoBuilder.init()
        AttributeDataFromBazaar.init()
    }
}
