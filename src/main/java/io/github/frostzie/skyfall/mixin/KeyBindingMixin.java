package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.features.garden.GardenKeybinds;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class KeyBindingMixin {

    @Inject(method = "wasPressed", at = @At("HEAD"), cancellable = true)
    private void onWasPressed(CallbackInfoReturnable<Boolean> cir) {
        GardenKeybinds.isKeyPressed((KeyBinding)(Object)this, cir);
    }

    @Inject(method = "isPressed", at = @At("HEAD"), cancellable = true)
    private void onIsPressed(CallbackInfoReturnable<Boolean> cir) {
        GardenKeybinds.isKeyDown((KeyBinding)(Object)this, cir);
    }
}