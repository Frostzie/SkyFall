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
        logger.info("DEBUG: Attempting to register HUD feature: ${feature::class.simpleName}")

        if (!initialized) {
            logger.info("DEBUG: HUD system not initialized, initializing now...")
            initialize()
        }

        if (registeredFeatures.add(feature)) {
            logger.info("DEBUG: Successfully registered HUD feature: ${feature::class.simpleName}")
            logger.info("DEBUG: Total HUD features registered: ${registeredFeatures.size}")
        } else {
            logger.warn("DEBUG: HUD feature ${feature::class.simpleName} was already registered")
        }
    }

    fun unregisterFeature(feature: IHudRenderable) {
        if (registeredFeatures.remove(feature)) {
            logger.info("DEBUG: Unregistered HUD feature: ${feature::class.simpleName}")
            logger.info("DEBUG: Remaining HUD features: ${registeredFeatures.size}")
        }
    }

    private fun initialize() {
        if (initialized) {
            logger.warn("DEBUG: HUD event system already initialized, skipping...")
            return
        }

        logger.info("DEBUG: Initializing HUD event system...")

        try {
            EventBus.listen(HudRenderEvent.Pre::class.java) { event ->
                logger.debug("DEBUG: HUD Pre-render event received, dispatching to ${registeredFeatures.size} features")
                registeredFeatures.forEach { feature ->
                    if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                        try {
                            feature.onHudPreRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD pre-render for ${feature::class.simpleName}: ${e.message}")
                        }
                    } else if (feature !is IFeature) {
                        // Handle features that implement IHudRenderable but not IFeature
                        try {
                            feature.onHudPreRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD pre-render for ${feature::class.simpleName}: ${e.message}")
                        }
                    }
                }
            }

            EventBus.listen(HudRenderEvent.Main::class.java) { event ->
                logger.debug("DEBUG: HUD Main-render event received, dispatching to ${registeredFeatures.size} features")
                registeredFeatures.forEach { feature ->
                    if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                        try {
                            feature.onHudRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD render for ${feature::class.simpleName}: ${e.message}")
                        }
                    } else if (feature !is IFeature) {
                        // Handle features that implement IHudRenderable but not IFeature
                        try {
                            feature.onHudRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD render for ${feature::class.simpleName}: ${e.message}")
                        }
                    }
                }
            }

            EventBus.listen(HudRenderEvent.Post::class.java) { event ->
                logger.debug("DEBUG: HUD Post-render event received, dispatching to ${registeredFeatures.size} features")
                registeredFeatures.forEach { feature ->
                    if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                        try {
                            feature.onHudPostRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD post-render for ${feature::class.simpleName}: ${e.message}")
                        }
                    } else if (feature !is IFeature) {
                        try {
                            feature.onHudPostRender(event)
                        } catch (e: Exception) {
                            logger.error("Error in HUD post-render for ${feature::class.simpleName}: ${e.message}")
                        }
                    }
                }
            }

            initialized = true
            logger.info("DEBUG: HUD event system successfully initialized")
        } catch (e: Exception) {
            logger.error("Failed to initialize HUD event system", e)
            throw e
        }
    }

    /**
     * Force initialization of the event system
     * Call this during client startup to ensure events are ready
     */
    fun ensureInitialized() {
        if (!initialized) {
            logger.info("DEBUG: Force initializing HUD event system...")
            initialize()
        }
    }

    /**
     * Get debug information about the current state
     */
    fun getDebugInfo(): Map<String, Any> {
        return mapOf(
            "initialized" to initialized,
            "registered_features" to registeredFeatures.size,
            "feature_names" to registeredFeatures.map { it::class.simpleName }
        )
    }
}