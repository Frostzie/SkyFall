package io.github.frostzie.skyfall.utils.power

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.TabListUpdateEvent
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.mining.WhisperChangeEvent
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.LoggerProvider

/**
 * Tracks whisper amounts exclusively from the tab list for improved accuracy.
 */
object WhisperTracking {
    private val TAB_WHISPERS_PATTERN = "(?:§.)*Forest Whispers: (?:§.)*(?<amount>.*)".toRegex()

    private val logger = LoggerProvider.getLogger("WhisperTracking")

    private var lastTabWhispers: String? = null
    private var currentWhisperAmount: String? = null

    /**
     * Initializes the whisper tracker by listening for tab list update events.
     */
    init {
        EventBus.listen(TabListUpdateEvent::class.java) { event ->
            lastTabWhispers = extractWhispersFromTabList(event.tabList)
            updateWhisperState()
        }
    }

    /**
     * Updates the whisper state based on tab list data. If data is lost,
     * it sends a helpful warning to the user.
     */
    private fun updateWhisperState() {
        val newWhisperAmount = lastTabWhispers

        if (newWhisperAmount != currentWhisperAmount) {
            val oldAmount = currentWhisperAmount
            currentWhisperAmount = newWhisperAmount

            val relevantIslands = listOf(IslandType.THE_PARK, IslandType.GALATEA)
            val currentIsland = IslandDetector.getCurrentIsland()

            if (oldAmount != null && newWhisperAmount == null && currentIsland.isOneOf(*relevantIslands.toTypedArray())) {
                ChatUtils.messageToChat("Could not find Forest Whispers. Ensure it's enabled in /tab settings.")
                    .clickToRun("/tab")
                    .send()
            }

            logger.info("Whisper amount changed: $oldAmount -> $newWhisperAmount")

            WhisperChangeEvent(oldAmount, newWhisperAmount).post()
        }
    }

    /**
     * Extracts whisper amount from tab list data.
     */
    private fun extractWhispersFromTabList(tabList: List<String>): String? {
        return tabList.firstNotNullOfOrNull { line ->
            TAB_WHISPERS_PATTERN.find(line)
                ?.groups?.get("amount")?.value?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.also { logger.info("Found tab whispers: $it") }
        }
    }

    /**
     * Gets the current whisper amount.
     */
    fun getCurrentWhisperAmount(): String? = currentWhisperAmount

    /**
     * Checks if whisper tracking is currently available.
     */
    fun hasWhisperData(): Boolean = currentWhisperAmount != null

    /**
     * Gets the raw whisper amount as a number (attempts to parse k/m suffixes).
     */
    fun getCurrentWhisperAmountAsNumber(): Long? {
        val amount = currentWhisperAmount ?: return null
        return parseWhisperNumber(amount)
    }

    /**
     * Parses whisper number strings like "17.5k", "141", "2.3m" into actual numbers.
     */
    private fun parseWhisperNumber(amount: String): Long? {
        try {
            val cleanAmount = amount.replace(",", "").lowercase()

            return when {
                cleanAmount.endsWith("k") -> {
                    val number = cleanAmount.dropLast(1).toDoubleOrNull() ?: return null
                    (number * 1000).toLong()
                }
                cleanAmount.endsWith("m") -> {
                    val number = cleanAmount.dropLast(1).toDoubleOrNull() ?: return null
                    (number * 1000000).toLong()
                }
                else -> cleanAmount.toLongOrNull()
            }
        } catch (e: Exception) {
            logger.info("Failed to parse whisper number: $amount - ${e.message}")
            return null
        }
    }
}