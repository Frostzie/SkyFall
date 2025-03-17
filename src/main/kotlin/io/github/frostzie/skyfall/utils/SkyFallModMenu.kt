package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.config.gui.ConfigGuiManager
import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screen.Screen

//TODO: fix this trowing out an error every time opening menu with ModMenu

class SkyFallModMenu : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<Screen> {
        return ConfigScreenFactory { ConfigGuiManager.openConfigGui() as Screen }
    }
}