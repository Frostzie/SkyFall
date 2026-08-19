package io.github.frostzie.skyfall.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerProvider {
    private val loggers = mutableMapOf<String, Logger>()

    fun getLogger(name: String): Logger {
        val fullName = "SkyFall:$name"
        return loggers.getOrPut(fullName) {
            LoggerFactory.getLogger(fullName)
        }
    }
}