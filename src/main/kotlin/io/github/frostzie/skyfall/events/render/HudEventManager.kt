package io.github.frostzie.skyfall.events.render

import io.github.frostzie.skyfall.api.feature.IFeature
import io.github.frostzie.skyfall.api.feature.IHudRenderable
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus

object HudEventManager {
    private val logger = LoggerProvider.getLogger("HudEventManager")
    private val registeredFeatures = mutableSetOf<IHudRenderable>()
    private var initialized = false

    fun registerFeature(feature: IHudRenderable) {
        if (registeredFeatures.add(feature)) {
            logger.info("DEBUG: Registered HUD feature: ${feature::class.simpleName}")
            if (!initialized) initialize()
        }
    }

    fun unregisterFeature(feature: IHudRenderable) {
        if (registeredFeatures.remove(feature)) {
            logger.info("DEBUG: Unregistered HUD feature: ${feature::class.simpleName}")
        }
    }

    private fun initialize() {
        if (initialized) return

        EventBus.listen(HudRenderEvent.Pre::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onHudPreRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in HUD pre-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(HudRenderEvent.Main::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onHudRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in HUD render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(HudRenderEvent.Post::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onHudPostRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in HUD post-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        initialized = true
        logger.info("HUD event system initialized")
    }
}