package io.github.frostzie.skyfall.mixin.features;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.frostzie.skyfall.SkyFall;
import io.github.frostzie.skyfall.util.ItemUtils;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemInHandRenderer.class)
public class ItemInHandRendererMixin {

    @Inject(method = "swingArm", at = @At("HEAD"), cancellable = true)
    private void skyfall$swingAnimation(float attack, PoseStack poseStack, int invert, HumanoidArm arm, CallbackInfo ci) {
        if (!SkyFall.features.getMisc().getAnimations().getDisableSwing()) return;

        ci.cancel();
    }

    @Inject(method = "shouldInstantlyReplaceVisibleItem", at = @At("HEAD"), cancellable = true)
    private void skyfall$forceInstantItemSwap(ItemStack currentlyVisibleItem, ItemStack expectedItem, CallbackInfoReturnable<Boolean> cir) {
        var config = SkyFall.features.getMisc().getAnimations();

        if (config.getDisableReSwing() &&
                ItemUtils.INSTANCE.compareUUID(currentlyVisibleItem, expectedItem)
        ) {
            cir.setReturnValue(true);
        } else if (config.getInstantItemSwap()) {
            cir.setReturnValue(true);
        }
    }
}
