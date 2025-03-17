package io.github.frostzie.skyfall.config.features.chat

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ChatConfig {

    @Expose
    @ConfigOption(name = "Color Helper", desc = "Sends a chat message with the color codes when running /sfcolor")
    @ConfigEditorBoolean
    var ColorCodeHelper: Boolean = false


    @Expose
    @ConfigOption(name = "Chat Test", desc = "Test if Chat Test is active")
    @ConfigEditorBoolean
    var ChatTest: Boolean = false

}