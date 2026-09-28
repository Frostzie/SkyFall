package io.github.frostzie.skyfall.mixin;

import io.github.frostzie.skyfall.SkyFall;
import io.github.frostzie.skyfall.events.GuiEvents;
import io.github.frostzie.skyfall.util.skyblock.PetUtilsKt;
import io.github.frostzie.skyfall.util.skyblock.SkyBlockMenusKt;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow protected int leftPos;
    @Shadow protected int topPos;
    @Final @Shadow protected int imageWidth;

    protected AbstractContainerScreenMixin(Component title) {
        super(title);
    }

    // Hopefully one day I can change this but that won't be today
    @Inject(method = "init", at = @At("TAIL"))
    private void skyfall$onInit(CallbackInfo ci) {
        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        var configPets = SkyFall.features.getPets().getFavoritePet();
        if (!configPets.getToggleButton()) {
            configPets.setFavOnlyToggle(false);
        } else if (PetUtilsKt.isPetMenu(screen)) {

            addRenderableWidget(
                    Button.builder(Component.literal("F"), _ -> {
                                boolean newVal = !configPets.getFavOnlyToggle();
                                configPets.setFavOnlyToggle(newVal);
                            })
                            .pos(leftPos + imageWidth - 18, topPos + 4)
                            .size(12, 12)
                            .build()
            );
        }

        var configAbi = SkyFall.features.getMisc().getAbiphone();
        if (!configAbi.getToggleButton()) {
            configAbi.setFavOnlyToggle(false);
        } else if (SkyBlockMenusKt.isAbiphoneMenu(screen)) {

            addRenderableWidget(
                    Button.builder(Component.literal("F"), _ -> {
                                boolean newVal = !configAbi.getFavOnlyToggle();
                                configAbi.setFavOnlyToggle(newVal);
                            })
                            .pos(leftPos + imageWidth - 18, topPos + 4)
                            .size(12, 12)
                            .build()
            );
        }
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

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void skyfall$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (hoveredSlot == null) return;
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) return;
        if (minecraft.options.keyInventory.matches(event)) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        if (GuiEvents.SLOT_CLICK_KEY.invoker().onSlotKeyClick(screen, hoveredSlot, event.key())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    private void skyfall$onMouseClicked(Slot slot, int slotId, int buttonNum, ContainerInput containerInput, CallbackInfo ci) {
        if (slot == null) return;

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;

        if (!GuiEvents.SLOT_CLICK.invoker().onSlotClick(screen, slot, buttonNum)) {
            ci.cancel();
        }
    }
}