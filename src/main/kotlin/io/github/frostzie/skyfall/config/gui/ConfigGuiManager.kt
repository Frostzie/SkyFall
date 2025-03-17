package io.github.frostzie.skyfall.config.gui

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.config.Features
import io.github.notenoughupdates.moulconfig.gui.GuiElementWrapper
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor


object ConfigGuiManager {
    var editor:  MoulConfigEditor<Features>? = null

    fun getEditorInstance() = editor ?: MoulConfigEditor(SkyFall.configManager.processor).also { editor = it }

    fun openConfigGui(search: String? = null) {
        val editor = getEditorInstance()
        if (search != null) {
            editor.search(search)
        }
        SkyFall.screenToOpen = GuiElementWrapper(editor)
    }
}