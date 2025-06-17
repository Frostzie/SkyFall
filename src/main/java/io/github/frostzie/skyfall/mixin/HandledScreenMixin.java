package io.github.frostzie.skyfall.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.frostzie.skyfall.utils.events.SlotRenderEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HandledScreen.class)
public class HandledScreenMixin<T extends ScreenHandler> {

    @WrapOperation(method = "drawSlots", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;drawSlot(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/screen/slot/Slot;)V"))
    public void onDrawSlots(HandledScreen<T> instance, DrawContext context, Slot slot, Operation<Void> original) {
        var before = new SlotRenderEvents.Before(context, slot);
        SlotRenderEvents.INSTANCE.publish(before);
        original.call(instance, context, slot);
    }
}