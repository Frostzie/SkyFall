package io.github.frostzie.skyfall

class ConfigAccessors {
    companion object {
        @JvmStatic
        fun hideBlockBreakingParticles(): Boolean {
            return SkyFall.Companion.feature.miscFeatures.blockBreakingParticles
        }

        @JvmStatic
        fun hidePotionEffectsHud(): Boolean {
            return SkyFall.Companion.feature.miscFeatures.hidePotionHud
        }
    }
}