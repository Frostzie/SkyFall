package io.github.frostzie.skyfall.api.feature

import io.github.frostzie.skyfall.events.inventory.SlotEventManager
import io.github.frostzie.skyfall.events.render.HudEventManager
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent
import io.github.frostzie.skyfall.hud.FeatureHudElement
import io.github.frostzie.skyfall.hud.HudElement
import io.github.frostzie.skyfall.hud.HudElementConfig
import io.github.frostzie.skyfall.hud.HudManager
import io.github.frostzie.skyfall.impl.fabric.FabricEventBridge
import net.minecraft.client.gui.DrawContext

// ===== SHARED COMPONENTS =====
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Feature(val name: String)

// Core feature interface - used by both systems
interface IFeature {
    var isRunning: Boolean
    fun shouldLoad(): Boolean
    fun init()
    fun terminate()
}

// ===== NEW EVENT SYSTEM INTERFACES =====
interface IHudRenderable {
    fun onHudPreRender(event: HudRenderEvent.Pre) {}
    fun onHudRender(event: HudRenderEvent.Main) {}
    fun onHudPostRender(event: HudRenderEvent.Post) {}
}

interface ISlotRenderable {
    fun onSlotPreRender(event: SlotRenderEvent.Pre) {}
    fun onSlotRender(event: SlotRenderEvent.Main) {}
    fun onSlotPostRender(event: SlotRenderEvent.Post) {}
}

interface ISlotInteractable {
    fun onSlotClick(event: SlotClickEvent) {}
}

//TODO: Chat
//TODO:

// ===== NEW SYSTEM BASE CLASSES =====
// Composite interface for features that need all event types

// Full event feature - registers with FeatureEventManager
abstract class EventFeature : IEventFeature {
    override var isRunning = false

    final override fun init() {
        if (isRunning) return
        FabricEventBridge.initialize()
        FeatureEventManager.registerFeature(this)
        isRunning = true
        onInit()
    }

    final override fun terminate() {
        if (!isRunning) return
        FeatureEventManager.unregisterFeature(this)
        isRunning = false
        onTerminate()
    }

    protected open fun onInit() {}
    protected open fun onTerminate() {}
}

/**
 * A specialized feature for rendering on the HUD.
 * It can now act as either:
 * 1. A global, static overlay (like a vignette).
 * 2. A user-configurable, movable element that appears in the HUD editor.
 *
 * This is controlled by the `isMovable` flag.
 */
abstract class HudFeature : IFeature, IHudRenderable {
    override var isRunning = false

    /**
     * If true, this feature will be registered as a movable element in the HUD editor.
     * If false (default), it will be a global overlay managed by HudEventManager.
     */
    open val isMovable: Boolean = false

    /**
     * The unique ID for the element in the HUD editor.
     * MUST be overridden if `isMovable` is true.
     */
    open val elementId: String get() = if (isMovable) throw NotImplementedError("Movable HudFeature must override elementId") else ""

    /**
     * The display name for the element in the HUD editor.
     * MUST be overridden if `isMovable` is true.
     */
    open val elementName: String get() = if (isMovable) throw NotImplementedError("Movable HudFeature must override elementName") else ""

    /**
     * The default position and size for the element in the HUD editor.
     * MUST be overridden if `isMovable` is true.
     */
    open val defaultElementConfig: HudElementConfig get() = if (isMovable) throw NotImplementedError("Movable HudFeature must override defaultElementConfig") else HudElementConfig(0,0,0,0)

    open val elementAdvancedSizing: Boolean = false
    open val elementMinWidth: Int? = null
    open val elementMinHeight: Int? = null

    /**
     * The render logic for a MOVABLE hud element.
     * This is called by the wrapper in HudManager.
     *
     * @param drawContext The context for drawing.
     * @param element The HudElement instance, containing current position and size in `element.config`.
     */
    open fun onMovableHudRender(drawContext: DrawContext, element: HudElement) {}

    private var managedHudElement: HudElement? = null

    final override fun init() {
        if (isRunning) return

        if (isMovable) {
            val element = FeatureHudElement(
                id = this.elementId,
                name = this.elementName,
                defaultConfig = this.defaultElementConfig,
                advancedSizingOverride = this.elementAdvancedSizing,
                minWidthOverride = this.elementMinWidth,
                minHeightOverride = this.elementMinHeight,
                renderAction = { drawContext, elem -> this.onMovableHudRender(drawContext, elem) }
            )
            this.managedHudElement = element
            HudManager.registerElement(element)
        } else {
            FabricEventBridge.initialize()
            HudEventManager.registerFeature(this)
        }

        isRunning = true
        onInit()
    }

    final override fun terminate() {
        if (!isRunning) return

        if (isMovable) {
            managedHudElement?.let { HudManager.unregisterElement(it.id) }
            managedHudElement = null
        } else {
            HudEventManager.unregisterFeature(this)
        }

        isRunning = false
        onTerminate()
    }

    protected open fun onInit() {}
    protected open fun onTerminate() {}
}

// Specialized Slot-only feature - registers with SlotEventManager
abstract class SlotFeature : IFeature, ISlotRenderable, ISlotInteractable {
    override var isRunning = false

    final override fun init() {
        if (isRunning) return
        FabricEventBridge.initialize()
        SlotEventManager.registerFeature(this)
        isRunning = true
        onInit()
    }

    final override fun terminate() {
        if (!isRunning) return
        SlotEventManager.unregisterFeature(this)
        isRunning = false
        onTerminate()
    }

    protected open fun onInit() {}
    protected open fun onTerminate() {}
}

// ===== LEGACY SYSTEM BASE CLASS =====
// Simple feature for legacy/custom event handling
abstract class SimpleFeature : IFeature {
    override var isRunning = false

    final override fun init() {
        if (isRunning) return
        isRunning = true
        onInit()
    }

    final override fun terminate() {
        if (!isRunning) return
        isRunning = false
        onTerminate()
    }

    protected open fun onInit() {}
    protected open fun onTerminate() {}
}