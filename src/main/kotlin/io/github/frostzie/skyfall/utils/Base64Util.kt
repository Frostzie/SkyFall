package io.github.frostzie.skyfall.utils

class Base64Util {
    /**
     * Adds padding to Base64 strings that are missing it
     */
    fun padToValidBase64(original: String): String {
        when (original.length % 4) {
            1 -> return original + "==="
            2 -> return original + "=="
            3 -> return original + "="
            else -> return original
        }
    }

    companion object {
        @JvmField
        var INSTANCE: Base64Util = Base64Util()
    }
}