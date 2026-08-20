package io.github.frostzie.skyfall.compat

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import io.github.frostzie.skyfall.config.ConfigGuiManager
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

class ModMenuIntegration : ModMenuApi {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> {
        return ConfigScreenFactory<Screen> { prevScreen ->
            createConfigScreen(ConfigGuiManager.getEditorInstance(), prevScreen)
        }
    }

    private fun createConfigScreen(editor: MoulConfigEditor<*>, prevScreen: Screen? = null) =
        MoulConfigScreenComponent(Component.empty(), GuiContext(GuiElementComponent(editor)), prevScreen)
}