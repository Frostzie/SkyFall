// Your MouseSensitivityMixin.java (which is in Java)
package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.features.garden.MouseSensitivity; // This calls the Kotlin object's static method
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseSensitivityMixin {

    @Inject(method = "onCursorPos", at = @At("HEAD"), cancellable = true)
    private void skyfall_onCursorPosPreventMovement(long window, double x, double y, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null && client.getWindow().getHandle() == window) {
            if (MouseSensitivity.shouldCancelMouseMovement()) {
                ci.cancel();
            }
        }
    }
}