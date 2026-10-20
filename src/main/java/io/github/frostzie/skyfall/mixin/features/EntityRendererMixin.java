package io.github.frostzie.skyfall.mixin.features;

import io.github.frostzie.skyfall.SkyFall;
import io.github.frostzie.skyfall.core.MobStore;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    //? if 26.3
    private void onShouldRender(Entity entity, Frustum culler, double camX, double camY, double camZ, float partialTicks, CallbackInfoReturnable<Boolean> cir) {
    //? if 26.2
    //private void onShouldRender(Entity entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (SkyFall.features.getMisc().getDeadRendering()
                && entity instanceof LivingEntity livingEntity
                && !livingEntity.isAlive()) {

            cir.setReturnValue(false);
        }

        if (MobStore.INSTANCE.isHidden(entity.getId())) {
            cir.setReturnValue(false);
        }
    }
}
