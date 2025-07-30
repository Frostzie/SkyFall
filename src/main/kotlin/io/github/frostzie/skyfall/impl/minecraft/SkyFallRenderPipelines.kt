package io.github.frostzie.skyfall.impl.minecraft

import com.mojang.blaze3d.pipeline.RenderPipeline
import net.minecraft.client.gl.RenderPipelines

/**
 * Provides a centralized, abstract access point to vanilla Minecraft's rendering objects.
 *
 * By routing all calls through this bridge, we can easily adapt to changes in future
 * Minecraft versions. If a render pipeline is renamed or its implementation changes,
 * we only need to update it in this one location, rather than across the entire codebase.
 *
 * The pipelines are organized into nested objects for clarity (e.g., `Gui`, `Entity`).
 */
object SkyfallRenderPipelines {

    /** Pipelines related to user interface elements, text, and screen overlays. */
    object Gui {
        /** @see RenderPipelines.GUI */
        val GUI: RenderPipeline = RenderPipelines.GUI
        /** @see RenderPipelines.GUI_TEXTURED */
        val GUI_TEXTURED: RenderPipeline = RenderPipelines.GUI_TEXTURED
        /** @see RenderPipelines.RENDERTYPE_TEXT */
        val TEXT: RenderPipeline = RenderPipelines.RENDERTYPE_TEXT
        /** @see RenderPipelines.VIGNETTE */
        val VIGNETTE: RenderPipeline = RenderPipelines.VIGNETTE
        /** @see RenderPipelines.CROSSHAIR */
        val CROSSHAIR: RenderPipeline = RenderPipelines.CROSSHAIR
    }

    /** Pipelines related to rendering in-world entities. */
    object Entity {
        /** @see RenderPipelines.ENTITY_SOLID */
        val SOLID: RenderPipeline = RenderPipelines.ENTITY_SOLID
        /** @see RenderPipelines.ENTITY_CUTOUT */
        val CUTOUT: RenderPipeline = RenderPipelines.ENTITY_CUTOUT
        /** @see RenderPipelines.ENTITY_CUTOUT_NO_CULL */
        val CUTOUT_NO_CULL: RenderPipeline = RenderPipelines.ENTITY_CUTOUT_NO_CULL
        /** @see RenderPipelines.ENTITY_TRANSLUCENT */
        val TRANSLUCENT: RenderPipeline = RenderPipelines.ENTITY_TRANSLUCENT
        /** @see RenderPipelines.RENDERTYPE_ENTITY_SHADOW */
        val SHADOW: RenderPipeline = RenderPipelines.RENDERTYPE_ENTITY_SHADOW
    }

    /** Pipelines related to rendering world blocks and terrain. */
    object Block {
        /** @see RenderPipelines.SOLID */
        val SOLID: RenderPipeline = RenderPipelines.SOLID
        /** @see RenderPipelines.CUTOUT */
        val CUTOUT: RenderPipeline = RenderPipelines.CUTOUT
        /** @see RenderPipelines.CUTOUT_MIPPED */
        val CUTOUT_MIPPED: RenderPipeline = RenderPipelines.CUTOUT_MIPPED
        /** @see RenderPipelines.TRANSLUCENT */
        val TRANSLUCENT: RenderPipeline = RenderPipelines.TRANSLUCENT
        /** @see RenderPipelines.RENDERTYPE_CRUMBLING */
        val CRUMBLING: RenderPipeline = RenderPipelines.RENDERTYPE_CRUMBLING
        /** @see RenderPipelines.RENDERTYPE_WATER_MASK */
        val WATER_MASK: RenderPipeline = RenderPipelines.RENDERTYPE_WATER_MASK
    }

    /** Pipelines for various visual effects and miscellaneous rendering tasks. */
    object Effect {
        /** @see RenderPipelines.GLINT */
        val GLINT: RenderPipeline = RenderPipelines.GLINT
        /** @see RenderPipelines.LINES */
        val LINES: RenderPipeline = RenderPipelines.LINES
        /** @see RenderPipelines.RENDERTYPE_LIGHTNING */
        val LIGHTNING: RenderPipeline = RenderPipelines.RENDERTYPE_LIGHTNING
        /** @see RenderPipelines.END_PORTAL */
        val END_PORTAL: RenderPipeline = RenderPipelines.END_PORTAL
        /** @see RenderPipelines.WEATHER_NO_DEPTH */
        val WEATHER: RenderPipeline = RenderPipelines.WEATHER_NO_DEPTH
    }
}