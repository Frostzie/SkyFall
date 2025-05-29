package io.github.frostzie.skyfall.utils

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.frostzie.skyfall.config.ConfigGuiManager

class SkyFallModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory { parent ->
            ConfigGuiManager.createModMenuScreen(parent)
        }
    }
}