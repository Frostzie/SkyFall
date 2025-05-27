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
    }
}