package io.github.frostzie.skyfall.config.features

import com.google.gson.annotations.Expose
import io.github.frostzie.skyfall.core.BoxStyle
import io.github.notenoughupdates.moulconfig.ChromaColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorColour
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MobHighlight {

    @Expose
    @ConfigOption(name = "Enable", desc = "Main toggle to enable custom mob highlighting.\nSet mobs with /skyfallHighlight...\n§cDoes not highlight behind objects/blocks!")
    @ConfigEditorBoolean
    var enabled: Boolean = false

    @Expose
    @ConfigOption(name = "Color", desc = "Color to highlight the mobs.")
    @ConfigEditorColour
    var color: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 255)

    @Expose
    @ConfigOption(name = "Outline Color", desc = "Color of the outline of the highlighted box.")
    @ConfigEditorColour
    var outlineColor: ChromaColour = ChromaColour.fromRGB(255, 255, 255, 0, 255)

    @Expose
    @ConfigOption(name = "Disable Model", desc = "Disables highlighted mobs model from being rendered. (Makes the hitbox be the only highlighted thing.)")
    @ConfigEditorBoolean
    var model: Boolean = false

    @Expose
    @ConfigOption(name = "Style", desc = "The style the mob should be highlighted.")
    @ConfigEditorDropdown
    var style: BoxStyle = BoxStyle.FILLED

    @Expose
    @ConfigEditorDraggableList
    var hypixelMobs: MutableSet<String> = mutableSetOf()

    @Expose
    @ConfigEditorDraggableList
    var vanillaMobs: MutableSet<String> = mutableSetOf()
}