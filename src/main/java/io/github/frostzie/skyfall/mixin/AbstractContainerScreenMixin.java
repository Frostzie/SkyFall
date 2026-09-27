package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.SkyFall;
import io.github.frostzie.skyfall.events.GuiEvents;
import io.github.frostzie.skyfall.util.skyblock.PetUtilsKt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Final @Shadow protected int imageWidth;

    // For the time being I think it's fine to build it here but in future factory it is
    @Inject(method = "init", at = @At("TAIL"))
    private void skyfall$addButton(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        if (!PetUtilsKt.isPetMenu(self)) return;
        var config = SkyFall.features.getPets().getFavoritePet();
        if (!config.getToggleButton()) {
            config.setFavOnlyToggle(false);
            return;
        }

        int x = leftPos + imageWidth - 12 - 6;
        int y = topPos + 4;

        Button skyfall$button = Button.builder(Component.literal("F"), _ -> {
                    boolean newVal = !config.getFavOnlyToggle();
                    config.setFavOnlyToggle(newVal);

                    SkyFall.configManager.save();
                })
                .bounds(x, y, 12, 12)
                .build();
        ((ScreenInvoker) this).skyfall$addRenderableWidget(skyfall$button);
    }

    @Inject(method = "extractSlot", at = @At("HEAD"), cancellable = true)
    private void skyfall$extractSlot(GuiGraphicsExtractor graphics, Slot slot, int mouseX, int mouseY, CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        var context = GuiEvents.SLOT_RENDER.invoker().getContext(screen, slot);
        int color = context.getHighlightColor();

        if (color != 0) {
            int x = slot.x;
            int y = slot.y;
            graphics.fill(x, y, x + 16, y + 16, color);
        }

        if (!context.getRenderSlot()) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTooltip", at = @At("HEAD"), cancellable = true)
    private void skyfall$onDrawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (hoveredSlot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        if (!GuiEvents.SLOT_RENDER.invoker().getContext(screen, hoveredSlot).getRenderTooltip()) {
            ci.cancel();
        }
    }
}