package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.utils.events.TooltipEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public class TooltipMixin {

    @Inject(method = "getTooltipFromItem", at = @At("RETURN"), cancellable = true)
    private static void onGetTooltipFromItem(MinecraftClient minecraftClient, ItemStack itemStack, CallbackInfoReturnable<List<Text>> cir) {
        if (itemStack != null && !itemStack.isEmpty()) {
            List<Text> originalLines = cir.getReturnValue();
            if (originalLines == null) {
                return;
            }
            List<Text> mutableLines = new ArrayList<>(originalLines);
            TooltipEvents.INSTANCE.onTooltipRender(itemStack, mutableLines);
            cir.setReturnValue(mutableLines);
        }
    }
}