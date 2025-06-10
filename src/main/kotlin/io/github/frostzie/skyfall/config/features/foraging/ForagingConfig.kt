package io.github.frostzie.skyfall.config.features.foraging

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ForagingConfig {

    @Expose
    @Accordion
    @ConfigOption(name = "Tune Frequency Solver", desc = "")
    var tuneFrequencySolver: TuneFrequencySolver = TuneFrequencySolver()

    class TuneFrequencySolver {
        @Expose
        @ConfigOption(name = "Enabled", desc = "Enable the tune frequency solver to detect what color and sound level is needed.")
        @ConfigEditorBoolean
        var enabled: Boolean = false

        @Expose
        @ConfigOption(name = "Speed Solver", desc = "Speed Detection will be coming eventually but since currently the island is laggy getting accurate speed is hard.")
        @ConfigEditorInfoText(
            infoTitle = " "
        )
        var notice: String = ""
    }
}