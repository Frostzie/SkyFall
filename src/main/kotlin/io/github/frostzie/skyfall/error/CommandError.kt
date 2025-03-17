package io.github.frostzie.skyfall.error

class CommandError(message: String, couse: Throwable) : Error(message, couse)