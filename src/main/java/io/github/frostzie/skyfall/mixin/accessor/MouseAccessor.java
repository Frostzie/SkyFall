package io.github.frostzie.skyfall.mixin.accessor;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Mutable
    @Accessor("x")
    void skyfall_setMouseX(double xPos);

    @Mutable
    @Accessor("y")
    void skyfall_setMouseY(double yPos);

    @Mutable
    @Accessor("cursorDeltaX")
    void skyfall_setCursorDeltaX(double deltaX);

    @Mutable
    @Accessor("cursorDeltaY")
    void skyfall_setCursorDeltaY(double deltaY);
}