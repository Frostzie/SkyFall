package io.github.frostzie.skyfall.api.feature

import io.github.frostzie.skyfall.events.inventory.SlotEventManager
import io.github.frostzie.skyfall.impl.fabric.FabricEventBridge
import io.github.frostzie.skyfall.events.render.HudEventManager
import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent

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

// Specialized HUD-only feature - registers with HudEventManager
abstract class HudFeature : IFeature, IHudRenderable {
    override var isRunning = false

    final override fun init() {
        if (isRunning) return
        FabricEventBridge.initialize()
        HudEventManager.registerFeature(this)
        isRunning = true
        onInit()
    }

    final override fun terminate() {
        if (!isRunning) return
        HudEventManager.unregisterFeature(this)
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