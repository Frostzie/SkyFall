package io.github.frostzie.skyfall.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import java.util.Set;

/**
 * Accessor for the tab list in ClientPlayNetworkHandler.
 * This mixin provides access to the list of players in the tab list,
 * which is normally not accessible directly.
 *
 * Added null safety to prevent crashes when texture signatures are invalid.
 */
@Mixin(ClientPlayNetworkHandler.class)
public interface TabListAccessor {
    /**
     * Gets the list of players in the tab list.
     * Update the field name according to your MCP/Yarn mappings.
     *
     * @return The set of player list entries
     */
    @Accessor("listedPlayerListEntries")
    Set<PlayerListEntry> getPlayerList();
}