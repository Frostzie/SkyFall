package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.ConfigAccessors;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Taken and modified from Tweakeroo
@Mixin(ParticleManager.class)
public abstract class BlockParticlesMixin {
    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void onAddBlockDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (ConfigAccessors.hideBlockBreakingParticles()) {
            ci.cancel();
        }
    }
}