package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.ConfigAccessors;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudEffectsMixin {

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void disableStatusEffectOverlay(CallbackInfo ci) {
        if (ConfigAccessors.hidePotionEffectsHud()) {
            ci.cancel();
        }
    }
}