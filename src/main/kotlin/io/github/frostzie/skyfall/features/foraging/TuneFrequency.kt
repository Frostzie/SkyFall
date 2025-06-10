package io.github.frostzie.skyfall.features.foraging

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.utils.SoundUtils
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.sound.SoundInstance
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.registry.Registries
import net.minecraft.screen.slot.Slot
import java.util.concurrent.ConcurrentHashMap

object TuneFrequency {
    private val correctSlotRanges = setOf(
        10..16,
    )

    private val activeNotes = ConcurrentHashMap<String, NoteInfo>()

    private val config get() = SkyFall.feature.foraging.tuneFrequencySolver

    data class NoteInfo(
        val noteName: String,
        val timestamp: Long
    )

    fun init() {
        ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
            if (screen is GenericContainerScreen && config.enabled) {
                ScreenEvents.afterRender(screen).register { _, context, mouseX, mouseY, delta ->
                    renderTuneFrequencyBox(context, screen)
                }
            }
        }
    }

    fun detectTuneFrequencyNote(soundInstance: SoundInstance): String? {
        val noteInfo = SoundUtils.createNoteInfo(soundInstance)
        return when (noteInfo.noteValue) {
            "F#3" -> "Low"
            "C#3" -> "Normal"
            "C4" -> "High"
            else -> null
        }
    }

    fun onSoundPlay(soundInstance: SoundInstance) {
        if (!config.enabled) return

        val frequencyLevel = detectTuneFrequencyNote(soundInstance)
        if (frequencyLevel == null) {
            return
        }

        activeNotes[frequencyLevel] = NoteInfo(
            noteName = frequencyLevel,
            timestamp = System.currentTimeMillis()
        )
    }

    fun onSoundStop(soundInstance: SoundInstance) {
        if (!config.enabled) return

        val frequencyLevel = detectTuneFrequencyNote(soundInstance)

        if (frequencyLevel != null) {
            activeNotes.remove(frequencyLevel)
        }
    }

    private fun isTuneFrequencyScreen(screen: HandledScreen<*>): Boolean {
        val title = screen.title.string
        return title.equals("Tune Frequency")
    }

    private fun isSlotInChestInventory(slot: Slot): Boolean {
        return slot.inventory !is PlayerInventory
    }

    private fun renderTuneFrequencyBox(context: DrawContext, screen: HandledScreen<*>) {
        if (!config.enabled || !isTuneFrequencyScreen(screen)) {
            return
        }

        val client = MinecraftClient.getInstance()
        val screenWidth = client.window.scaledWidth
        val screenHeight = client.window.scaledHeight

        val boxWidth = 150
        val boxHeight = 110
        val boxX = (screenWidth - screen.backgroundWidth) / 2 - boxWidth - 10
        val boxY = (screenHeight - screen.backgroundHeight) / 2

        val backgroundColor = 0x000000
        context.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, backgroundColor)

        val borderColor = 0xFF404040.toInt()
        context.drawBorder(boxX, boxY, boxWidth, boxHeight, borderColor)

        val textRenderer = client.textRenderer
        val titleText = "Solver"
        val titleX = boxX + (boxWidth - textRenderer.getWidth(titleText)) / 2
        val titleY = boxY + 8
        context.drawText(textRenderer, titleText, titleX, titleY, 0xFFFFFF, true)

        val optionsStartY = titleY + 16
        val lineHeight = 12
        val textX = boxX + 8

        val inventory = screen.screenHandler.slots
        val isPaused = inventory.getOrNull(51)?.stack?.item?.translationKey == "block.minecraft.red_terracotta"

        val currentTime = System.currentTimeMillis()
        val maxDisplayTime = if (isPaused) 1000L else 7000L

        activeNotes.entries.removeIf { (_, info) ->
            currentTime - info.timestamp > maxDisplayTime
        }

        val detectedNotes = activeNotes.values.map { it.noteName }.distinct()

        val soundText = when {
            detectedNotes.size > 1 -> "Pause To Detect"
            detectedNotes.size == 1 -> detectedNotes.first()
            else -> "Not Detected"
        }

        val glassPaneName = inventory
            .filter { slot -> isSlotInChestInventory(slot) && correctSlotRanges.any { slot.index in it } }
            .mapNotNull { slot -> slot.stack }
            .firstOrNull { stack ->
                val id = Registries.ITEM.getId(stack.item).toString()
                id.startsWith("minecraft:") && id.contains("stained_glass_pane") && !id.contains("gray") && !id.contains("light_gray")
            }?.item?.translationKey
            ?.replace("block.minecraft.", "")
            ?.replace("_stained_glass_pane", "")
            ?.split("_")
            ?.joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } } ?: "None"

        val options = mutableListOf(
            "Color: $glassPaneName",
            "Sound: $soundText",
            "Speed: Eventually™"
        )

        options.forEachIndexed { index, option ->
            val textY = optionsStartY + (index * lineHeight)
            context.drawText(textRenderer, option, textX, textY, 0xCCCCCC, true)
        }
    }
}