package io.github.frostzie.skyfall.utils

import java.util.Base64

// Taken from Firmament
object Base64Util {
    fun decodeString(str: String): String {
        return Base64.getDecoder().decode(str.padToValidBase64())
            .decodeToString()
    }

    fun String.padToValidBase64(): String {
        val align = this.length % 4
        if (align == 0) return this
        return this + "=".repeat(4 - align)
    }
}