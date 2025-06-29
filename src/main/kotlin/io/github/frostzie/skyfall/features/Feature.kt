package io.github.frostzie.skyfall.features

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Feature(val name: String)

// Based on the system used by Skyhanni
interface IFeature {
    /**
     * A flag to track if the feature is currently active.
     * This should be set to true in init() and false in terminate().
     */
    var isRunning: Boolean

    /**
     * The condition to check if this feature should be loaded.
     * This will now be checked both at startup and after config changes.
     */
    fun shouldLoad(): Boolean

    /**
     * The entry point of the feature. Called when the feature is enabled.
     * Should register listeners and set isRunning = true.
     */
    fun init()

    /**
     * A new method to clean up the feature when it's disabled.
     * Should unregister listeners, reset state, and set isRunning = false.
     */
    fun terminate()
}