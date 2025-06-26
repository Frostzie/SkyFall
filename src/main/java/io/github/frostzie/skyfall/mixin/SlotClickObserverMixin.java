// Based on Firmament's SlotClickEventPatch.java
// Original: https://github.com/nea89o/Firmament/blob/master/src/main/java/moe/nea/firmament/mixins/SlotClickEventPatch.java

package io.github.frostzie.skyfall.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.frostzie.skyfall.utils.item.SlotHandler;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
class SlotClickObserverMixin {

    @Inject(method = "clickSlot", at = @At(value = "FIELD", target = "Lnet/minecraft/screen/ScreenHandler;slots:Lnet/minecraft/util/collection/DefaultedList;", opcode = Opcodes.GETFIELD))
    private void onSlotClick_savePreState(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, @Local ScreenHandler handler, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotId && slotId < handler.slots.size()) {
            slotContent.set(handler.getSlot(slotId).getStack().copy());
        } else {
            slotContent.set(ItemStack.EMPTY.copy());
        }
    }

    @Inject(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void onSlotClick_firePostEvent(int syncId, int slotId, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci, @Local ScreenHandler handler, @Share("slotContent") LocalRef<ItemStack> slotContent) {
        if (0 <= slotId && slotId < handler.slots.size()) {
            SlotHandler.firePostClickEvent(
                    handler.getSlot(slotId),
                    slotContent.get(),
                    button,
                    actionType
            );
        }
    }
}