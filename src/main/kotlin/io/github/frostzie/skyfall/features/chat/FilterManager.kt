package io.github.frostzie.skyfall.features.chat

import io.github.frostzie.skyfall.features.chat.filters.HideFireSaleMessage
import io.github.frostzie.skyfall.features.chat.filters.HideSkyMallChanges
import io.github.frostzie.skyfall.features.chat.filters.HideWatchdogMessage

object FilterManager {
    fun loadFilters() {
        HideWatchdogMessage.init()
        HideFireSaleMessage.init()
        HideSkyMallChanges.init()
    }
}