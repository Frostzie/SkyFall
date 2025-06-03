package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.ConfigAccessors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectsDisplay.class)
public abstract class MixinHidePotionHud {
    private MixinHidePotionHud()
    {
        super();
    }

    @Inject(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"), cancellable = true)
    private void disableStatusEffectRendering(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (ConfigAccessors.hidePotionEffectsHud()) {
            ci.cancel();
        }
    }
}