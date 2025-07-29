package io.github.frostzie.skyfall.utils.power

import io.github.frostzie.skyfall.data.IslandType
import io.github.frostzie.skyfall.events.TabListUpdateEvent
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.mining.PowderAmounts
import io.github.frostzie.skyfall.events.mining.PowderChangeEvent
import io.github.frostzie.skyfall.utils.ChatUtils
import io.github.frostzie.skyfall.utils.IslandDetector
import io.github.frostzie.skyfall.utils.LoggerProvider

/**
 * Tracks powder amounts exclusively from the tab list for improved accuracy.
 */
object PowderTracking {
    private val TAB_MITHRIL_PATTERN = "(?:§.)*Mithril: (?:§.)*(?<amount>.*)".toRegex()
    private val TAB_GEMSTONE_PATTERN = "(?:§.)*Gemstone: (?:§.)*(?<amount>.*)".toRegex()
    private val TAB_GLACITE_PATTERN = "(?:§.)*Glacite: (?:§.)*(?<amount>.*)".toRegex()

    private val logger = LoggerProvider.getLogger("PowderTracking")

    private var lastTabMithril: String? = null
    private var lastTabGemstone: String? = null
    private var lastTabGlacite: String? = null

    private var currentMithrilAmount: String? = null
    private var currentGemstoneAmount: String? = null
    private var currentGlaciteAmount: String? = null

    /**
     * Initializes the powder tracker by listening for tab list update events.
     */
    init {
        EventBus.listen(TabListUpdateEvent::class.java) { event ->
            lastTabMithril = extractPowderFromTabList(event.tabList, TAB_MITHRIL_PATTERN, "Mithril")
            lastTabGemstone = extractPowderFromTabList(event.tabList, TAB_GEMSTONE_PATTERN, "Gemstone")
            lastTabGlacite = extractPowderFromTabList(event.tabList, TAB_GLACITE_PATTERN, "Glacite")
            updatePowderState()
        }
    }

    /**
     * Updates the powder state based on tab list data. If data is lost,
     * it sends a helpful warning to the user.
     */
    private fun updatePowderState() {
        val newMithrilAmount = lastTabMithril
        val newGemstoneAmount = lastTabGemstone
        val newGlaciteAmount = lastTabGlacite
        val relevantIslands = listOf(IslandType.DWARVEN_MINES, IslandType.CRYSTAL_HOLLOWS, IslandType.MINESHAFT)
        val currentIsland = IslandDetector.getCurrentIsland()
        val shouldWarn = currentIsland.isOneOf(*relevantIslands.toTypedArray())

        var hasChanges = false

        if (newMithrilAmount != currentMithrilAmount) {
            val oldAmount = currentMithrilAmount
            currentMithrilAmount = newMithrilAmount
            hasChanges = true

            if (oldAmount != null && newMithrilAmount == null && shouldWarn) {
                ChatUtils.messageToChat("Could not find Mithril powder. Ensure it's enabled in /tab settings.")
                    .clickToRun("/tab")
                    .send()
            }

            logger.info("Mithril amount changed: $oldAmount -> $newMithrilAmount")
        }

        if (newGemstoneAmount != currentGemstoneAmount) {
            val oldAmount = currentGemstoneAmount
            currentGemstoneAmount = newGemstoneAmount
            hasChanges = true

            if (oldAmount != null && newGemstoneAmount == null && shouldWarn) {
                ChatUtils.messageToChat("Could not find Gemstone powder. Ensure it's enabled in /tab settings.")
                    .clickToRun("/tab")
                    .send()
            }

            logger.info("Gemstone amount changed: $oldAmount -> $newGemstoneAmount")
        }

        if (newGlaciteAmount != currentGlaciteAmount) {
            val oldAmount = currentGlaciteAmount
            currentGlaciteAmount = newGlaciteAmount
            hasChanges = true

            if (oldAmount != null && newGlaciteAmount == null && shouldWarn) {
                ChatUtils.messageToChat("Could not find Glacite powder. Ensure it's enabled in /tab settings.")
                    .clickToRun("/tab")
                    .send()
            }

            logger.info("Glacite amount changed: $oldAmount -> $newGlaciteAmount")
        }

        if (hasChanges) {
            PowderChangeEvent(
                PowderAmounts(currentMithrilAmount, currentGemstoneAmount, currentGlaciteAmount)
            ).post()
        }
    }

    /**
     * Extracts powder amount from tab list data using the specified pattern.
     */
    private fun extractPowderFromTabList(tabList: List<String>, pattern: Regex, powderType: String): String? {
        return tabList.firstNotNullOfOrNull { line ->
            pattern.find(line)
                ?.groups?.get("amount")?.value?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?.also { logger.info("Found tab $powderType: $it") }
        }
    }

    /**
     * Gets the current Mithril amount.
     */
    fun getCurrentMithrilAmount(): String? = currentMithrilAmount

    /**
     * Gets the current Gemstone amount.
     */
    fun getCurrentGemstoneAmount(): String? = currentGemstoneAmount

    /**
     * Gets the current Glacite amount.
     */
    fun getCurrentGlaciteAmount(): String? = currentGlaciteAmount

    /**
     * Gets all current powder amounts.
     */
    fun getCurrentPowderAmounts(): PowderAmounts {
        return PowderAmounts(currentMithrilAmount, currentGemstoneAmount, currentGlaciteAmount)
    }

    /**
     * Checks if powder tracking is currently available for any powder type.
     */
    fun hasPowderData(): Boolean = currentMithrilAmount != null || currentGemstoneAmount != null || currentGlaciteAmount != null

    /**
     * Checks if powder tracking is available for a specific powder type.
     */
    fun hasMithrilData(): Boolean = currentMithrilAmount != null
    fun hasGemstoneData(): Boolean = currentGemstoneAmount != null
    fun hasGlaciteData(): Boolean = currentGlaciteAmount != null

    /**
     * Gets the raw Mithril amount as a number (attempts to parse k/m suffixes and commas).
     */
    fun getCurrentMithrilAmountAsNumber(): Long? {
        val amount = currentMithrilAmount ?: return null
        return parsePowderNumber(amount)
    }

    /**
     * Gets the raw Gemstone amount as a number (attempts to parse k/m suffixes and commas).
     */
    fun getCurrentGemstoneAmountAsNumber(): Long? {
        val amount = currentGemstoneAmount ?: return null
        return parsePowderNumber(amount)
    }

    /**
     * Gets the raw Glacite amount as a number (attempts to parse k/m suffixes and commas).
     */
    fun getCurrentGlaciteAmountAsNumber(): Long? {
        val amount = currentGlaciteAmount ?: return null
        return parsePowderNumber(amount)
    }

    /**
     * Parses powder number strings like "17.5k", "141", "2.3m", "99,918,777" into actual numbers.
     */
    private fun parsePowderNumber(amount: String): Long? {
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
            logger.info("Failed to parse powder number: $amount - ${e.message}")
            return null
        }
    }
}