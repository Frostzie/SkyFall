package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.feature.pets.ActivePet;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Inject(method = "extractSlotHighlightBack", at = @At("HEAD"), cancellable = true)
    private void skyfall$drawCustomHighlight(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        ActivePet.INSTANCE.drawHighlight(graphics, (AbstractContainerScreen<?>) (Object) this);
        ci.cancel();
    }
}