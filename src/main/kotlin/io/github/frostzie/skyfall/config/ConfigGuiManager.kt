package io.github.frostzie.skyfall.config

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.SkyFall.Companion.screenToOpen
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.network.chat.Component

object ConfigGuiManager {
    var editor: MoulConfigEditor<Features>? = null

    fun getEditorInstance() = editor ?: MoulConfigEditor(SkyFall.configManager.processor).also { editor = it }

    fun openConfigGui(search: String?) {
        val editor = getEditorInstance()

        if (search != null) {
            editor.search(search)
        }

        openEditor(editor)
    }

    private fun openEditor(editor: MoulConfigEditor<*>) {
        screenToOpen = MoulConfigScreenComponent(Component.empty(), GuiContext(GuiElementComponent(editor)), null)
    }
}
