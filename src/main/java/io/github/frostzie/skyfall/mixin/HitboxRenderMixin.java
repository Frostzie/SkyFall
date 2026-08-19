package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.feature.CustomHitbox;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class HitboxRenderMixin {
    @Inject(
            method = "submitFeatures(Lnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/SubmitNodeCollector;Z)V",
            at = @At("TAIL")
    )
    private void skyfallHitbox$submit(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci) {
        //CustomHitbox.INSTANCE.submitHitboxes(levelRenderState.cameraRenderState, submitNodeCollector);
    }
}