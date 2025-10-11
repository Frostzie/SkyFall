package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.SkyFall
import io.github.notenoughupdates.moulconfig.common.text.StructuredText
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor
import io.github.notenoughupdates.moulconfig.gui.GuiContext
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent
import net.minecraft.text.Text

// Taken from SkyHanni
object ConfigUtils {

    fun openEditor(editor: MoulConfigEditor<*>) {
        SkyFall.screenToOpen = MoulConfigScreenComponent(Text.empty(), GuiContext(GuiElementComponent(editor)), null)
    }

    fun String.asStructuredText() = StructuredText.of(this)
}