// FeatureManager.kt - Enhanced to work with both old and new systems
package io.github.frostzie.skyfall.api.feature

import io.github.frostzie.skyfall.utils.LoggerProvider
import org.reflections.Reflections
import kotlin.system.measureTimeMillis

/**
 * Manages the lifecycle of all features in the client using reflection discovery.
 *
 * This manager automatically discovers features annotated with @Feature and handles
 * both the new event-based system and legacy features that manage their own events.
 */
object FeatureManager {
    private val logger = LoggerProvider.getLogger("FeatureManager")
    private val discoveredFeatures = mutableListOf<Pair<IFeature, String>>()

    fun initialize() {
        logger.info("Starting feature scan...")
        val timeInMillis = measureTimeMillis {
            try {
                val reflections = Reflections("io.github.frostzie.skyfall.features")
                val featureClasses = reflections.getTypesAnnotatedWith(Feature::class.java)
                logger.info(" Found ${featureClasses.size} potential features.")

                if (featureClasses.isNotEmpty()) {
                    val classNames = featureClasses.joinToString(separator = ", ") { it.simpleName }
                    logger.info(" Discovered features: [$classNames]")
                }

                for (featureClass in featureClasses) {
                    val annotation = featureClass.getAnnotation(Feature::class.java)
                    try {
                        val featureInstance = featureClass.kotlin.objectInstance
                        if (featureInstance is IFeature) {
                            discoveredFeatures.add(featureInstance to annotation.name)

                            // Log what type of feature this is for debugging
                            val capabilities = mutableListOf<String>()
                            when (featureInstance) {
                                is EventFeature -> capabilities.add("Full Event System")
                                is HudFeature -> capabilities.add("HUD Events")
                                is SlotFeature -> capabilities.add("Slot Events")
                                is SimpleFeature -> capabilities.add("Legacy/Custom")
                                else -> {
                                    // Handle other implementations
                                    if (featureInstance is IHudRenderable) capabilities.add("HUD")
                                    if (featureInstance is ISlotRenderable) capabilities.add("Slot Rendering")
                                    if (featureInstance is ISlotInteractable) capabilities.add("Slot Interaction")
                                    if (capabilities.isEmpty()) capabilities.add("Unknown")
                                }
                            }

                            logger.info(" Feature '${annotation.name}' type: [${capabilities.joinToString()}]")
                        } else {
                            logger.warn("Class ${featureClass.simpleName} is annotated with @Feature but does not implement IFeature.")
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to get instance of feature: '${annotation.name}' (${featureClass.simpleName})", e)
                    }
                }

                updateFeatureStates()
            } catch (e: Exception) {
                logger.error("A critical error occurred during feature scanning. No features will be loaded.", e)
            }
        }
        logger.info("Feature initialization completed in ${timeInMillis}ms.")
    }

    /**
     * Updates all discovered features - both new event system and legacy features
     */
    fun updateFeatureStates() {
        logger.info(" Updating feature states...")
        if (discoveredFeatures.isEmpty()) {
            logger.warn("No features were discovered. Cannot update states. Was initialize() called?")
            return
        }

        for ((feature, featureName) in discoveredFeatures) {
            try {
                val shouldBeRunning = feature.shouldLoad()
                val isActuallyRunning = feature.isRunning

                if (shouldBeRunning && !isActuallyRunning) {
                    logger.info(" -> Starting feature: '$featureName'")
                    feature.init() // This handles registration automatically based on feature type
                } else if (!shouldBeRunning && isActuallyRunning) {
                    logger.info(" -> Stopping feature: '$featureName'")
                    feature.terminate() // This handles unregistration automatically
                }
            } catch (e: Exception) {
                logger.error("Failed to change state for feature '$featureName'", e)
            }
        }

        logActiveFeatures()
        logger.info(" Finished updating feature states.")
    }

    private fun logActiveFeatures() {
        val activeFeatures = discoveredFeatures
            .filter { (feature, _) -> feature.isRunning }
            .groupBy { (feature, _) ->
                when (feature) {
                    is EventFeature -> "Full Event System"
                    is HudFeature -> "HUD Event System"
                    is SlotFeature -> "Slot Event System"
                    is SimpleFeature -> "Legacy/Custom"
                    else -> "Other"
                }
            }

        activeFeatures.forEach { (type, features) ->
            val names = features.joinToString(", ") { (_, name) -> name }
            logger.info(" Active $type features: [$names]")
        }

        if (activeFeatures.isEmpty()) {
            logger.info(" No features are currently enabled.")
        }
    }

    /**
     * Get statistics about the discovered features
     */
    fun getFeatureStats(): Map<String, Int> {
        return mapOf(
            "total" to discoveredFeatures.size,
            "running" to discoveredFeatures.count { (feature, _) -> feature.isRunning },
            "new_event_system" to discoveredFeatures.count { (feature, _) ->
                feature is EventFeature || feature is HudFeature || feature is SlotFeature
            },
            "legacy_system" to discoveredFeatures.count { (feature, _) -> feature is SimpleFeature },
            "hud_capable" to discoveredFeatures.count { (feature, _) -> feature is IHudRenderable },
            "slot_capable" to discoveredFeatures.count { (feature, _) ->
                feature is ISlotRenderable || feature is ISlotInteractable
            }
        )
    }
}