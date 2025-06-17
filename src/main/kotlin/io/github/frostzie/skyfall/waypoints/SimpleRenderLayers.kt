package io.github.frostzie.skyfall.waypoints

import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.DepthTestFunction
import com.mojang.blaze3d.vertex.VertexFormat
import io.github.frostzie.skyfall.mixin.accessor.RenderPipelinesAccessor
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.RenderPhase
import net.minecraft.client.render.VertexFormats
import net.minecraft.util.Identifier

object SimpleRenderLayers {
    private val wallsSnippet = RenderPipelinesAccessor.getPositionColorSnippet()

    private val blockCullPipeline = RenderPipeline.builder(wallsSnippet)
        .withLocation(Identifier.of("skyfall", "pipeline/block_cull"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .build()

    private val blockNoCullPipeline = RenderPipeline.builder(wallsSnippet)
        .withLocation(Identifier.of("skyfall", "pipeline/block_no_cull"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false)
        .build()

    private val areaCullPipeline = RenderPipeline.builder(wallsSnippet)
        .withLocation(Identifier.of("skyfall", "pipeline/area_cull"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .build()

    private val areaNoCullPipeline = RenderPipeline.builder(wallsSnippet)
        .withLocation(Identifier.of("skyfall", "pipeline/area_no_cull"))
        .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
        .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
        .withCull(false)
        .build()

    private val registeredBlockCull = RenderPipelinesAccessor.registerPipeline(blockCullPipeline)
    private val registeredBlockNoCull = RenderPipelinesAccessor.registerPipeline(blockNoCullPipeline)
    private val registeredAreaCull = RenderPipelinesAccessor.registerPipeline(areaCullPipeline)
    private val registeredAreaNoCull = RenderPipelinesAccessor.registerPipeline(areaNoCullPipeline)

    private val wallsParameters = RenderLayer.MultiPhaseParameters.builder()
        .layering(RenderPhase.VIEW_OFFSET_Z_LAYERING)
        .build(false)

    val BLOCK: RenderLayer.MultiPhase = RenderLayer.of(
        "skyfall_block",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        registeredBlockCull,
        wallsParameters
    )

    val BLOCK_NO_CULL: RenderLayer.MultiPhase = RenderLayer.of(
        "skyfall_block_no_cull",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        registeredBlockNoCull,
        wallsParameters
    )

    val AREA: RenderLayer.MultiPhase = RenderLayer.of(
        "skyfall_area",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        registeredAreaCull,
        wallsParameters
    )

    val AREA_NO_CULL: RenderLayer.MultiPhase = RenderLayer.of(
        "skyfall_area_no_cull",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        registeredAreaNoCull,
        wallsParameters
    )

    val BLOCK_FRONT_CULL: RenderLayer.MultiPhase = RenderLayer.of(
        "skyfall_block_front_cull",
        RenderLayer.DEFAULT_BUFFER_SIZE,
        false,
        true,
        RenderPipeline.builder(wallsSnippet)
            .withLocation(Identifier.of("skyfall", "pipeline/block_front_cull"))
            .withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withCull(true)
            .build(),
        wallsParameters
    )
}