package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.utils.ConfigAccessors;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ParticleManager.class)
public abstract class MixinBlockParticles {
    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void onAddBlockDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (ConfigAccessors.hideBlockBreakingParticles()) { // Use class name
            ci.cancel();
        }
    }
}