package io.github.frostzie.skyfall.utils

import net.minecraft.client.MinecraftClient
import net.minecraft.client.sound.SoundInstance
import net.minecraft.client.sound.SoundSystem
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import kotlin.math.log2

object SoundUtils {

    /**
     * Extracts the instrument name from a note block sound ID
     */
    fun extractNoteBlockInstrument(soundId: String): String {
        return when {
            soundId.contains("harp") -> "Harp"
            soundId.contains("bass") -> "Bass"
            soundId.contains("basedrum") -> "Bass Drum"
            soundId.contains("snare") -> "Snare"
            soundId.contains("hat") -> "Hi-Hat"
            soundId.contains("bell") -> "Bell"
            soundId.contains("flute") -> "Flute"
            soundId.contains("chime") -> "Chime"
            soundId.contains("guitar") -> "Guitar"
            soundId.contains("xylophone") -> "Xylophone"
            soundId.contains("iron_xylophone") -> "Iron Xylophone"
            soundId.contains("cow_bell") -> "Cow Bell"
            soundId.contains("didgeridoo") -> "Didgeridoo"
            soundId.contains("bit") -> "Bit"
            soundId.contains("banjo") -> "Banjo"
            soundId.contains("pling") -> "Pling"
            else -> "Piano"
        }
    }

    /**
     * Converts pitch value to musical note notation
     */
    fun pitchToNote(pitch: Float): String {
        val notes = arrayOf("F#", "G", "G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F")
        val semitones = (12 * log2(pitch / 0.5)).toInt()
        val clampedSemitones = semitones.coerceIn(0, 24)
        val noteIndex = clampedSemitones % 12
        val octave = 3 + (clampedSemitones / 12)

        return "${notes[noteIndex]}$octave"
    }

    /**
     * Checks if a sound is a note block sound
     */
    fun isNoteBlockSound(soundInstance: SoundInstance): Boolean {
        return soundInstance.id.toString().contains("note_block")
    }

    /**
     * Checks if a sound is a music-related sound (note blocks, music discs, background music)
     */
    fun isMusicSound(soundInstance: SoundInstance): Boolean {
        val soundId = soundInstance.id.toString()
        return soundId.contains("note_block") ||
                soundId.contains("music_disc") ||
                soundId.contains("music.")
    }

    /**
     * Gets the raw sound ID as a string
     */
    fun getSoundId(soundInstance: SoundInstance): String {
        return soundInstance.id.toString()
    }

    /**
     * Creates a simple sound identifier for internal use
     */
    fun createSoundKey(soundInstance: SoundInstance): String {
        return "${soundInstance.id}_${soundInstance.pitch}_${soundInstance.volume}"
    }

    /**
     * Gets the note value if it's a note block sound, null otherwise
     */
    fun getNoteValue(soundInstance: SoundInstance): String? {
        return if (isNoteBlockSound(soundInstance)) {
            pitchToNote(soundInstance.pitch)
        } else null
    }

    /**
     * Gets the instrument name if it's a note block sound, null otherwise
     */
    fun getInstrument(soundInstance: SoundInstance): String? {
        return if (isNoteBlockSound(soundInstance)) {
            extractNoteBlockInstrument(getSoundId(soundInstance))
        } else null
    }

    /**
     * Data class to hold basic sound information for notes
     */
    data class NoteInfo(
        val noteValue: String?,
        val instrument: String?,
        val pitch: Float,
        val volume: Float,
        val soundId: String
    )

    /**
     * Creates a NoteInfo object from a SoundInstance (focused on musical data)
     */
    fun createNoteInfo(soundInstance: SoundInstance): NoteInfo {
        return NoteInfo(
            noteValue = getNoteValue(soundInstance),
            instrument = getInstrument(soundInstance),
            pitch = soundInstance.pitch,
            volume = soundInstance.volume,
            soundId = getSoundId(soundInstance)
        )
    }

    /**
     * Plays a sound based on the given parameters.
     *
     * @param soundEvent the sound event to play.
     * @param category the sound category.
     * @param volume the volume of the sound.
     * @param pitch the pitch of the sound.
     */
    fun playSound(soundEvent: SoundEvent, category: SoundCategory, volume: Float = 1.0f, pitch: Float = 1.0f) {
        val client = MinecraftClient.getInstance()
        client.world?.let { world ->
            val player = client.player ?: return
            world.playSound(
                player,
                player.blockPos,
                soundEvent,
                category,
                volume,
                pitch
            )
        }
    }
}