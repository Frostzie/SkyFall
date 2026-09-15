package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.events.SlotKeyPressDispatch;
import io.github.frostzie.skyfall.events.SlotClickDispatch;
import io.github.frostzie.skyfall.events.SlotRenderContext;
import io.github.frostzie.skyfall.events.SlotRenderDispatch;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.inventory.ContainerInput;
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

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void skyfall$extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object)this;

        SlotRenderContext context = SlotRenderDispatch.process(graphics, screen, slot);

        if (!context.getShouldRenderSlot()) {
            ci.cancel();
            return;
        }

        if (context.getHighlightColor() != 0) {
            int x = slot.x;
            int y = slot.y;
            graphics.fill(x, y, x+ 16, y + 16, context.getHighlightColor());
        }
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void skyfall$onDrawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (hoveredSlot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object)this;
        SlotRenderContext context = SlotRenderDispatch.process(graphics, screen, hoveredSlot);

        if (!context.getShouldRenderTooltips()) {
            ci.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void skyfall$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (SlotKeyPressDispatch.dispatch(screen, hoveredSlot, event.key())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void skyfall$onSlotClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (slot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>)(Object)this;
        if (SlotClickDispatch.dispatch(screen, slot, buttonNum)) {
            ci.cancel();
        }
    }
}