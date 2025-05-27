package io.github.frostzie.skyfall.features.chat

import io.github.frostzie.skyfall.features.chat.filters.HideWatchdogMessage

object FilterManager {
    fun loadFilters() {
        HideWatchdogMessage.init()
    }
}