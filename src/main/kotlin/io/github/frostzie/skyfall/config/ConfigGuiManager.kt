package io.github.frostzie.skyfall.config

import io.github.frostzie.skyfall.SkyFall
import io.github.notenoughupdates.moulconfig.gui.GuiElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import net.minecraft.client.gui.screen.Screen

// Taken and modified from Skyhanni
object ConfigGuiManager {
    var editor: MoulConfigEditor<Features>? = null

    fun getEditorInstance(): MoulConfigEditor<Features> {
        if (editor == null) {
            editor = MoulConfigEditor(SkyFall.Companion.configManager.processor)
        }
        return editor!!
    }

    fun openConfigGui(search: String? = null) {
        val currentEditor = getEditorInstance()
        if (search != null) {
            currentEditor.search(search)
        }
        SkyFall.Companion.screenToOpen = GuiElementWrapper(currentEditor)
    }

    fun createModMenuScreen(parentScreenFromModMenu: Screen): Screen {
        val currentEditor = getEditorInstance()
        val configScreen = GuiElementWrapper(currentEditor)
        return configScreen
    }
}