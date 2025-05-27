package io.github.frostzie.skyfall.utils

import io.github.frostzie.skyfall.SkyFall.Companion.feature

class ConfigAccessors {
    companion object {
        @JvmStatic
        fun hideBlockBreakingParticles(): Boolean {
            return feature.miscFeatures.blockBreakingParticles
        }

        @JvmStatic
        fun hidePotionEffectsHud(): Boolean {
            return feature.miscFeatures.hidePotionHud
        }
    }
}