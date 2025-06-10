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
    @ConfigOption(name = "Location Command", desc = "Enable the /sfDevIslandType command to show your current island name and area.")
    @ConfigEditorBoolean
    var locationCommand: Boolean = false

    @Expose
    @ConfigOption(name = "Sound Detector", desc = "Enable the sound detector feature to see all sounds played in the game.")
    @ConfigEditorBoolean
    var soundDetector: Boolean = false
}