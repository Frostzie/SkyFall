package io.github.frostzie.skyfall.api.feature

import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent

/**
 * Automatic event registration manager for the NEW event system features only.
 *
 * This manager handles EventFeature instances (full event features) and provides
 * centralized event dispatching to all registered features.
 *
 * Legacy features (SimpleFeature) handle their own event registration separately.
 */
object FeatureEventManager {
    private val logger = LoggerProvider.getLogger("FeatureEventManager")
    private val registeredFeatures = mutableSetOf<IEventFeature>()
    private var initialized = false

    /**
     * Register a feature for automatic event handling
     * Only called by EventFeature.init()
     */
    fun registerFeature(feature: IEventFeature) {
        if (registeredFeatures.add(feature)) {
            logger.info("Registered feature for events: ${feature::class.simpleName}")

            // Initialize event listeners on first registration
            if (!initialized) {
                initializeEventListeners()
                initialized = true
            }
        }
    }

    /**
     * Unregister a feature from automatic event handling
     * Only called by EventFeature.terminate()
     */
    fun unregisterFeature(feature: IEventFeature) {
        if (registeredFeatures.remove(feature)) {
            logger.info("Unregistered feature from events: ${feature::class.simpleName}")
        }
    }

    /**
     * Initialize the event system - registers all event listeners with EventBus
     * This is called lazily when the first EventFeature is registered
     */
    fun initializeEventListeners() {
        // HUD Render Events
        EventBus.listen(HudRenderEvent.Pre::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature.isRunning && feature.shouldLoad()) {
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
                if (feature.isRunning && feature.shouldLoad()) {
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
                if (feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onHudPostRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in HUD post-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        // Slot Render Events
        EventBus.listen(SlotRenderEvent.Pre::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotPreRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot pre-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(SlotRenderEvent.Main::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(SlotRenderEvent.Post::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotPostRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot post-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        // Slot Interaction Events
        EventBus.listen(SlotClickEvent::class.java) { event ->
            registeredFeatures.forEach { feature ->
                if (feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotClick(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot click for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        logger.info("Feature event system initialized with ${registeredFeatures.size} features")
    }

    /**
     * Get current statistics for debugging
     */
    fun getStats(): Map<String, Int> {
        return mapOf(
            "registered_features" to registeredFeatures.size,
            "active_features" to registeredFeatures.count { it.isRunning },
            "initialized" to if (initialized) 1 else 0
        )
    }
}