package io.github.frostzie.skyfall.error

class ConfigError(message: String, couse: Throwable) : Error(message, couse)