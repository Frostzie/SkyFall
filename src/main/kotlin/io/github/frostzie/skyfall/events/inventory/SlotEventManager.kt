package io.github.frostzie.skyfall.events.inventory

import io.github.frostzie.skyfall.api.feature.IFeature
import io.github.frostzie.skyfall.api.feature.ISlotInteractable
import io.github.frostzie.skyfall.api.feature.ISlotRenderable
import io.github.frostzie.skyfall.utils.LoggerProvider
import io.github.frostzie.skyfall.events.core.EventBus
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent

object SlotEventManager {
    private val logger = LoggerProvider.getLogger("SlotEventManager")
    private val renderableFeatures = mutableSetOf<ISlotRenderable>()
    private val interactableFeatures = mutableSetOf<ISlotInteractable>()
    private var initialized = false

    fun registerFeature(feature: Any) {
        var registered = false

        if (feature is ISlotRenderable) {
            if (renderableFeatures.add(feature)) {
                logger.info("DEBUG: Registered slot renderable feature: ${feature::class.simpleName}")
                registered = true
            }
        }

        if (feature is ISlotInteractable) {
            if (interactableFeatures.add(feature)) {
                logger.info("DEBUG: Registered slot interactable feature: ${feature::class.simpleName}")
                registered = true
            }
        }

        if (registered && !initialized) initialize()
    }

    fun unregisterFeature(feature: Any) {
        var unregistered = false

        if (feature is ISlotRenderable) {
            if (renderableFeatures.remove(feature)) {
                logger.info("DEBUG: Unregistered slot renderable feature: ${feature::class.simpleName}")
                unregistered = true
            }
        }

        if (feature is ISlotInteractable) {
            if (interactableFeatures.remove(feature)) {
                logger.info("DEBUG: Unregistered slot interactable feature: ${feature::class.simpleName}")
                unregistered = true
            }
        }
    }

    private fun initialize() {
        if (initialized) return

        // Register slot render events
        EventBus.listen(SlotRenderEvent.Pre::class.java) { event ->
            renderableFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotPreRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot pre-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(SlotRenderEvent.Main::class.java) { event ->
            renderableFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        EventBus.listen(SlotRenderEvent.Post::class.java) { event ->
            renderableFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotPostRender(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot post-render for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        // Register slot click events
        EventBus.listen(SlotClickEvent::class.java) { event ->
            interactableFeatures.forEach { feature ->
                if (feature is IFeature && feature.isRunning && feature.shouldLoad()) {
                    try {
                        feature.onSlotClick(event)
                    } catch (e: Exception) {
                        logger.error("Error in slot click for ${feature::class.simpleName}: ${e.message}")
                    }
                }
            }
        }

        initialized = true
        logger.info("Slot event system initialized")
    }
}