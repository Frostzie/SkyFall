package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.events.SlotKeyPressDispatch;
import io.github.frostzie.skyfall.events.SlotRenderDispatch;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(method = "extractSlot", at = @At("HEAD"))
    private void skyfall$drawCustomHighlight(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object)this;
        SlotRenderDispatch.process(graphics, screen, slot);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void skyfall$onKeyPressed(KeyEvent keyEvent, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (SlotKeyPressDispatch.dispatch(screen, hoveredSlot, keyEvent.key())) {
            cir.setReturnValue(true);
        }
    }
}