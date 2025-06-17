package io.github.frostzie.skyfall.mixin.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gl.RenderPipelines;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RenderPipelines.class)
public interface RenderPipelinesAccessor {
    @Accessor("POSITION_COLOR_SNIPPET")
    static RenderPipeline.Snippet getPositionColorSnippet() {
        return null;
    }

    @Invoker("register")
    static RenderPipeline registerPipeline(RenderPipeline pipeline) {
        return null;
    }
}