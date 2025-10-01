package io.github.frostzie.skyfall.features.dev

import io.github.frostzie.skyfall.SkyFall
import io.github.frostzie.skyfall.api.feature.Feature
import io.github.frostzie.skyfall.api.feature.IEventFeature
import io.github.frostzie.skyfall.utils.SoundUtils
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.sound.SoundInstance
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min

@Feature(name = "Sound Detector")
object SoundDetector : IEventFeature {
    override var isRunning = false

    private val activeSounds = ConcurrentHashMap<String, SoundInfo>()
    private val mc = MinecraftClient.getInstance()
    private val config get() = SkyFall.feature.dev

    data class SoundInfo(
        val displayName: String,
        val timestamp: Long,
        val volume: Float,
        val pitch: Float,
        val soundId: String
    )

    init {
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.SUBTITLES,
            Identifier.of("skyfall", "sounds")
        ) { context, _ ->
            if (!isRunning) return@attachElementAfter
            renderSoundOverlay(context)
        }
    }

    override fun shouldLoad(): Boolean {
        return config.enabledDevMode && config.soundDetector
    }

    override fun init() {
        isRunning = true
    }

    override fun terminate() {
        isRunning = false
        clearAllSounds()
    }

    fun onSoundPlay(soundInstance: SoundInstance) {
        if (!isRunning) return

        val detailedName = createDetailedSoundName(soundInstance)
        val uniqueKey = "${detailedName}_${System.nanoTime()}"

        activeSounds[uniqueKey] = SoundInfo(
            displayName = detailedName,
            timestamp = System.currentTimeMillis(),
            volume = soundInstance.volume,
            pitch = soundInstance.pitch,
            soundId = SoundUtils.getSoundId(soundInstance)
        )
    }

    fun clearAllSounds() {
        activeSounds.clear()
    }

    fun onSoundStop(soundInstance: SoundInstance) {
    }

    private fun createDetailedSoundName(soundInstance: SoundInstance): String {
        val soundId = SoundUtils.getSoundId(soundInstance)
        val volume = soundInstance.volume
        val pitch = soundInstance.pitch

        return when {
            soundId.contains("note_block") -> {
                val instrument = SoundUtils.extractNoteBlockInstrument(soundId)
                val note = SoundUtils.pitchToNote(pitch)
                "♪ Note Block ($instrument) - $note"
            }

            // Music discs
            soundId.contains("music_disc") -> {
                val discName = soundId.substringAfterLast(".").replace("_", " ").uppercase()
                "♫ Music Disc: $discName"
            }

            // Background music
            soundId.contains("music.") -> {
                val musicType = soundId.substringAfterLast(".")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                "♫ Background Music: $musicType"
            }

            // UI sounds
            soundId.contains("ui.") -> {
                val uiSound = soundId.substringAfterLast(".")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                "🔊 UI: $uiSound"
            }

            // Entity sounds
            soundId.contains("entity.") -> {
                val parts = soundId.split(".")
                if (parts.size >= 3) {
                    val entity = parts[1].replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    val action = parts[2].replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    "🐾 $entity: $action"
                } else {
                    "🐾 ${soundId.replace("_", " ")}"
                }
            }

            // Block sounds
            soundId.contains("block.") -> {
                val parts = soundId.split(".")
                if (parts.size >= 3) {
                    val block = parts[1].replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    val action = parts[2].replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                    "🧱 $block: $action"
                } else {
                    "🧱 ${soundId.replace("_", " ")}"
                }
            }

            // Item sounds
            soundId.contains("item.") -> {
                val itemSound = soundId.substringAfter("item.")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                "📦 Item: $itemSound"
            }

            // Ambient sounds
            soundId.contains("ambient.") -> {
                val ambientSound = soundId.substringAfter("ambient.")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                "🌟 Ambient: $ambientSound"
            }

            // Weather sounds
            soundId.contains("weather.") -> {
                val weatherSound = soundId.substringAfter("weather.")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                "⛈️ Weather: $weatherSound"
            }

            // Generic sounds with pitch/volume info
            else -> {
                val baseName = soundId.substringAfterLast(":")
                    .replace("_", " ")
                    .split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }

                val pitchInfo = if (pitch != 1.0f) " (♪${String.format("%.2f", pitch)})" else ""
                val volumeInfo = if (volume != 1.0f) " [${String.format("%.1f", volume)}]" else ""

                "🔊 $baseName$pitchInfo$volumeInfo"
            }
        }
    }

    private fun renderSoundOverlay(drawContext: DrawContext) {
        if (mc.player == null || activeSounds.isEmpty()) return

        val currentTime = System.currentTimeMillis()
        val maxDisplayTime = 4000L

        activeSounds.entries.removeIf { (_, info) ->
            currentTime - info.timestamp > maxDisplayTime
        }

        if (activeSounds.isEmpty()) return

        val screenWidth = mc.window.scaledWidth
        val screenHeight = mc.window.scaledHeight
        val textRenderer = mc.textRenderer

        val soundList = activeSounds.values.sortedByDescending { it.timestamp }
        val maxSounds = min(10, soundList.size)

        var yOffset = screenHeight - 40

        for (i in 0 until maxSounds) {
            val sound = soundList[i]
            val age = currentTime - sound.timestamp
            val alpha = ((maxDisplayTime - age) / maxDisplayTime.toFloat()).coerceIn(0f, 1f)

            if (alpha <= 0f) continue

            val displayText = sound.displayName
            val textWidth = textRenderer.getWidth(displayText)
            val xPos = screenWidth - textWidth - 15

            val bgAlpha = (alpha * 0.7f * 255).toInt()
            val bgColor = (bgAlpha shl 24) or 0x000000
            drawContext.fill(xPos - 4, yOffset - 2, xPos + textWidth + 4, yOffset + 10, bgColor)

            val borderAlpha = (alpha * 0.9f * 255).toInt()
            val borderColor = (borderAlpha shl 24) or 0x404040
            drawContext.drawStrokedRectangle(xPos - 4, yOffset - 2, textWidth + 8, 12, borderColor)

            val textAlpha = (alpha * 255).toInt()
            val textColor = (textAlpha shl 24) or 0xFFFFFF
            drawContext.drawText(textRenderer, displayText, xPos, yOffset, textColor, false)

            yOffset -= 14
        }
    }
}