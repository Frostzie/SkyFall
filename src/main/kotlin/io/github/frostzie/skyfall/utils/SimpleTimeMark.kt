package io.github.frostzie.skyfall.utils

import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// Taken and modified from Skyhanni
@JvmInline
value class SimpleTimeMark(private val millis: Long) {
    operator fun minus(other: SimpleTimeMark) =
        (millis - other.millis).milliseconds

    operator fun plus(other: Duration) =
        SimpleTimeMark(millis + other.inWholeMilliseconds)

    fun passedSince() = if (millis == 0L) Duration.INFINITE else now() - this

    fun timeUntil() = -passedSince()

    fun isInPast() = timeUntil().isNegative()

    override fun toString(): String {
        if (millis == 0L) return "The Far Past"
        return Instant.ofEpochMilli(millis).toString()
    }

    fun toMillis() = millis

    companion object {
        fun now() = SimpleTimeMark(System.currentTimeMillis())
        fun farPast() = SimpleTimeMark(0)

        fun Long.asTimeMark() = SimpleTimeMark(this)
    }
}