package io.github.frostzie.skyfall.config.features.dev

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class DevConfig {
    @Expose
    @ConfigOption(name = "Enabled", desc = "Enable dev features")
    @ConfigEditorBoolean
    var enabledDevMode: Boolean = false

    @Expose
    @ConfigOption(name = "Config Save", desc = "Show config save messages in console")
    @ConfigEditorBoolean
    var showSaveConfigMessages: Boolean = false
}