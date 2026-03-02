package io.github.frostzie.nodex.domain.config

/**
 * Small runtime configuration model for universal setup.
 */
//TODO: replace with setting, just for temp
data class UniversalRuntimeConfig(
    val enabled: Boolean = false,
    val basePath: String = ""
)
