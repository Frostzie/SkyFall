package io.github.frostzie.skyfall.config

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.hud.HudEditorScreen
import io.github.notenoughupdates.moulconfig.gui.GuiElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import net.minecraft.client.gui.screen.Screen

object ConfigGuiManager {

    private var configEditor: MoulConfigEditor<Features>? = null

    var currentScreenInstance: Screen? = null

    private fun getConfigEditorInstance(): MoulConfigEditor<Features> {
        if (configEditor == null) {
            configEditor = MoulConfigEditor(SkyFall.configManager.processor)
        }
        return configEditor!!
    }

    fun openConfigGui(search: String? = null) {
        val currentEditor = getConfigEditorInstance()
        if (search != null) {
            currentEditor.search(search)
        }

        val screen = GuiElementWrapper(currentEditor)
        currentScreenInstance = screen
        SkyFall.screenToOpen = screen
    }

    fun openHudEditor() {
        val screen = HudEditorScreen()
        currentScreenInstance = screen
        SkyFall.screenToOpen = screen
    }

    fun createModMenuScreen(parentScreenFromModMenu: Screen): Screen {
        val newEditor = MoulConfigEditor(SkyFall.configManager.processor)
        return GuiElementWrapper(newEditor)
    }
}