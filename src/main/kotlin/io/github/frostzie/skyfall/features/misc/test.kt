package io.github.frostzie.skyfall.features.misc

import com.mojang.blaze3d.systems.RenderSystem
import io.github.frostzie.skyfall.SkyFall
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.render.*

object Test {
    fun test() {
        HudRenderCallback.EVENT.register(HudRenderCallback { drawContext: DrawContext?, tickDeltaManager: RenderTickCounter? ->
            if (!SkyFall.feature.miscFeatures.renderTriangle) {
                return@HudRenderCallback
            }

            val transformationMatrix = drawContext!!.getMatrices().peek().getPositionMatrix()
            val tessellator = Tessellator.getInstance()

            val buffer = tessellator.begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR)

            buffer.vertex(transformationMatrix, 20f, 20f, 5f).color(-0xbf0011)
            buffer.vertex(transformationMatrix, 5f, 40f, 5f).color(-0x1000000)
            buffer.vertex(transformationMatrix, 35f, 40f, 5f).color(-0x1000000)
            buffer.vertex(transformationMatrix, 20f, 60f, 5f).color(-0x00bf05)

            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)

            BufferRenderer.drawWithGlobalProgram(buffer.end())
        })
    }
}
