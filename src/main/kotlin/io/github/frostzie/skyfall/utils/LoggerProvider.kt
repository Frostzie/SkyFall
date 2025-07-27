package io.github.frostzie.skyfall.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private const val MAIN = "skyfall"

    fun getLogger(name: String): Logger {
        val fullName = "$MAIN:$name"
        return LoggerFactory.getLogger(fullName)
    }
}