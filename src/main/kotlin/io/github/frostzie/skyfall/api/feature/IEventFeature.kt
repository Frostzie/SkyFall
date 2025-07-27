package io.github.frostzie.skyfall.api.feature

import io.github.frostzie.skyfall.events.render.HudRenderEvent
import io.github.frostzie.skyfall.events.render.SlotClickEvent
import io.github.frostzie.skyfall.events.render.SlotRenderEvent

/**
 * This file is now deprecated - interfaces moved to Feature.kt
 *
 * This composite interface is kept for backward compatibility.
 * New features should extend EventFeature, HudFeature, or SlotFeature directly.
 */
@Deprecated("Use EventFeature, HudFeature, or SlotFeature from Feature.kt instead")
interface IEventFeature : IFeature {
    /**
     * Called when HUD should be rendered (Pre phase)
     */
    fun onHudPreRender(event: HudRenderEvent.Pre) {}

    /**
     * Called when HUD should be rendered (Main phase)
     */
    fun onHudRender(event: HudRenderEvent.Main) {}

    /**
     * Called when HUD should be rendered (Post phase)
     */
    fun onHudPostRender(event: HudRenderEvent.Post) {}

    /**
     * Called before slot rendering
     */
    fun onSlotPreRender(event: SlotRenderEvent.Pre) {}

    /**
     * Called during slot rendering (can modify appearance)
     */
    fun onSlotRender(event: SlotRenderEvent.Main) {}

    /**
     * Called after slot rendering
     */
    fun onSlotPostRender(event: SlotRenderEvent.Post) {}

    /**
     * Called when a slot is clicked
     */
    fun onSlotClick(event: SlotClickEvent) {}
}