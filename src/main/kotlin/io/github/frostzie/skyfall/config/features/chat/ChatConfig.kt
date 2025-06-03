package io.github.frostzie.skyfall.config.features.chat

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ChatConfig {

    @Expose
    @Accordion
    @ConfigOption(name = "Chat Filters", desc = "")
    var chatFilters: Filters = Filters()

    class Filters {
        @Expose
        @ConfigOption(name = "Whatchdog", desc = "Hides the watchdog announcement message in chat.")
        @ConfigEditorBoolean
        var hideWatchdog = false

        @Expose
        @ConfigOption(name = "Fire Sale", desc = "Hides the fire sale message in chat.")
        @ConfigEditorBoolean
        var hideFireSale = false

        @Expose
        @ConfigOption(name = "Sky Mall", desc = "Hides Sky Mall stat changes when not in mining islands.")
        @ConfigEditorBoolean
        var hideSkyMallChange = false
    }
}