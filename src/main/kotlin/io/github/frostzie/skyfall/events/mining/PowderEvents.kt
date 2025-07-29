package io.github.frostzie.skyfall.events.mining

import io.github.frostzie.skyfall.events.core.Event

/**
 * Data class to hold all powder amounts.
 */
data class PowderAmounts(
    val mithril: String?,
    val gemstone: String?,
    val glacite: String?
)

/**
 * Event fired when any powder amount changes.
 */
data class PowderChangeEvent(
    val powderAmounts: PowderAmounts
) : Event()

/**
 * Event fired when whisper amount changes.
 */
data class WhisperChangeEvent(
    val oldAmount: String?,
    val newAmount: String?
) : Event()