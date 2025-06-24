package io.github.frostzie.skyfall.error

class CommandError(message: String, cause: Throwable) : Error(message, cause)