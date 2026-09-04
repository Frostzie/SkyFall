package io.github.frostzie.skyfall.mixin.features;

import io.github.frostzie.skyfall.SkyFall;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.Holder;
import net.minecraft.server.dialog.Dialog;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin {

    @Inject(method = "addCustomDialogButtons", at = @At("HEAD"), cancellable = true)
    private void skyfall$hideServerDialogButton(Minecraft minecraft, Holder<Dialog> dialog, GridLayout.RowHelper helper, CallbackInfo ci) {
        if (SkyFall.features.getMisc().getPauseMenu().getHideServerLinkButton()) {
            ci.cancel();
        }
    }
}
