package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.ConfigAccessors;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Taken and modified from Tweakeroo
@Mixin(ClientWorld.class)
public abstract class BlockParticlesMixin {
    @Inject(method = "addBlockBreakParticles(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V", at = @At("HEAD"), cancellable = true)
    private void onAddBlockDestroyEffects(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (ConfigAccessors.hideBlockBreakingParticles()) {
            ci.cancel();
        }
    }
}