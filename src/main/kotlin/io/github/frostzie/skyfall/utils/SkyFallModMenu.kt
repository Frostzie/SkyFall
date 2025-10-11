package io.github.frostzie.skyfall.utils

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.gui.screen.Screen
import net.minecraft.text.Text

class SkyFallModMenu : ModMenuApi {
    // Taken from Skyhanni ConfigModMenuInterop.kt
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> {
            MoulConfigScreenComponent(
                Text.empty(), GuiContext(GuiElementComponent(ConfigGuiManager.getConfigEditorInstance())),
                null)
        }
    }
}