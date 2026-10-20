package io.github.frostzie.skyfall.mixin;

import com.mojang.authlib.properties.Property;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Taken from Odin
// https://github.com/odtheking/Odin/blob/main/src/main/java/com/odtheking/mixin/mixins/YggdrasilSignatureIgnoreMixin.java

//? if 26.2 {
/*import com.mojang.authlib.yggdrasil.YggdrasilServicesKeyInfo;
@Mixin(value = YggdrasilServicesKeyInfo.class, remap = false)
*///?} else {
import com.mojang.authlib.services.MinecraftServicesKeyInfo;
@Mixin(value = MinecraftServicesKeyInfo.class, remap = false)
//?}
public class YggdrasilSignatureIgnoreMixin {

    @Inject(method = "validateProperty", at = @At("HEAD"), cancellable = true, remap = false)
    public void validate(Property property, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }
}