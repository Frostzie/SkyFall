package io.github.frostzie.skyfall.mixin;

import com.mojang.authlib.SignatureState;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = YggdrasilMinecraftSessionService.class, remap = false)
public class PropertySignatureIgnorePatchForSession {
    @Inject(method = "getPropertySignatureState", at = @At("HEAD"), cancellable = true, remap = false)
    public void markEverythingAsSigned(Property property, CallbackInfoReturnable<SignatureState> cir) {
            cir.setReturnValue(SignatureState.SIGNED);
    }
}