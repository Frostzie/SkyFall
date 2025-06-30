package io.github.frostzie.skyfall.features

import io.github.frostzie.skyfall.utils.LoggerProvider
import org.reflections.Reflections
import kotlin.system.measureTimeMillis

// Based on the system used by SkyHanni
//TODO: Possibly add a hybrid system to allow faster startup times.
/**
 * Manages the lifecycle of all features in the client.
 *
 * This manager uses reflection to discover all features annotated with `@Feature`.
 * It can dynamically start and stop features based on the user's configuration,
 * allowing for changes to be applied without restarting the game.
 *
 * The main entry point is `initialize()`, which should be called once on startup.
 * `updateFeatureStates()` can be called anytime the configuration is saved to apply changes.
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
                logger.debug("Found ${featureClasses.size} potential features.")
                if (featureClasses.isNotEmpty()) {
                    val classNames = featureClasses.joinToString(separator = ", ") { it.simpleName }
                    logger.debug("Discovered features: [$classNames]")
                }

                for (featureClass in featureClasses) {
                    val annotation = featureClass.getAnnotation(Feature::class.java)
                    try {
                        val featureInstance = featureClass.kotlin.objectInstance
                        if (featureInstance is IFeature) {
                            discoveredFeatures.add(featureInstance to annotation.name)
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
     * Iterates over all discovered features and synchronizes their running state
     * with the desired state from the configuration.
     *
     * This method handles starting newly enabled features and stopping newly disabled ones.
     * It should be called after the configuration is changed/saved.
     */
    fun updateFeatureStates() {
        logger.debug("Updating feature states...")
        if (discoveredFeatures.isEmpty()) {
            logger.warn("No features were discovered. Cannot update states. Was initialize() called?")
            return
        }

        for ((feature, featureName) in discoveredFeatures) {
            try {
                val shouldBeRunning = feature.shouldLoad()
                val isActuallyRunning = feature.isRunning

                if (shouldBeRunning && !isActuallyRunning) {
                    logger.debug("-> Starting feature: '$featureName'")
                    feature.init()
                } else if (!shouldBeRunning && isActuallyRunning) {
                    logger.debug("-> Stopping feature: '$featureName'")
                    feature.terminate()
                }
            } catch (e: Exception) {
                logger.error("Failed to change state for feature '$featureName'", e)
            }
        }

        val activeFeatureNames = discoveredFeatures
            .filter { (feature, _) -> feature.isRunning }
            .joinToString(separator = ", ") { (_, name) -> name }

        if (activeFeatureNames.isNotBlank()) {
            logger.debug("Enabled features: [$activeFeatureNames]")
        } else {
            logger.debug("No features are currently enabled.")
        }
        logger.debug("Finished updating feature states.")
    }
}