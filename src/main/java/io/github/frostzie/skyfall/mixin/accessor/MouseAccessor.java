package io.github.frostzie.skyfall.mixin.accessor; // Adjust to your mixin accessor package

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Mutable
    @Accessor("x") // Corrected field name
    void skyfall_setMouseX(double xPos); // Parameter name can be whatever you like, e.g., xPos

    @Mutable
    @Accessor("y") // Corrected field name
    void skyfall_setMouseY(double yPos); // Parameter name can be whatever you like, e.g., yPos

    @Mutable
    @Accessor("cursorDeltaX")
    void skyfall_setCursorDeltaX(double deltaX);

    @Mutable
    @Accessor("cursorDeltaY")
    void skyfall_setCursorDeltaY(double deltaY);
}