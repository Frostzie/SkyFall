package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.utils.item.SlotHandler;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class SlotRenderingMixin<T extends ScreenHandler> extends Screen {

    protected SlotRenderingMixin(Text title) {
        super(title);
    }

    @Shadow
    @Final
    protected T handler;

    @Shadow
    public abstract Slot getSlotAt(double x, double y);

    @Inject(method = "drawSlot", at = @At("HEAD"), cancellable = true)
    public void drawSlot_Head(DrawContext context, Slot slot, CallbackInfo ci) {
        if (slot == null) return;
        if (SlotHandler.shouldHideSlot(slot)) {
            ci.cancel();
        }
    }

    @Redirect(method = "drawSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/slot/Slot;getStack()Lnet/minecraft/item/ItemStack;"))
    public ItemStack getStack_Redirect(Slot slot) {
        ItemStack replacement = SlotHandler.getReplacementStack(slot);
        return replacement != null ? replacement : slot.getStack();
    }

    @Inject(method = "drawMouseoverTooltip", at = @At("HEAD"), cancellable = true)
    public void drawMouseoverTooltip_Head(DrawContext context, int x, int y, CallbackInfo ci) {
        Slot hoveredSlot = getSlotAt(x, y);
        if (hoveredSlot != null && SlotHandler.shouldHideTooltip(hoveredSlot)) {
            ci.cancel();
        }
    }

    // Taken and modified from Firmament
    @Inject(method = "onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void onMouseClick_Head(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
        Text screenTitle = getTitle();
        ItemStack cursorStack = this.handler.getCursorStack();
        PlayerEntity player = this.client.player;

        if (SlotHandler.shouldBlockClick(slot, slotId, button, actionType, screenTitle, cursorStack, player)) {
            ci.cancel();
        }
    }
}