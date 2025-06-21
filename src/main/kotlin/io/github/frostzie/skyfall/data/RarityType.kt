package io.github.frostzie.skyfall.data

import io.github.frostzie.skyfall.utils.item.TooltipUtils
import net.minecraft.item.ItemStack

enum class RarityType(val formattedText: String) {
    COMMON("§f§lCOMMON"),
    UNCOMMON("§a§lUNCOMMON"),
    RARE("§9§lRARE"),
    EPIC("§5§lEPIC"),
    LEGENDARY("§6§lLEGENDARY"),
    MYTHIC("§d§lMYTHIC"),
    DIVINE("§b§lDIVINE"),
    SPECIAL("§c§lSPECIAL"),
    VERY_SPECIAL("§c§lVERY SPECIAL");

    companion object {
        /**
         * Detects the rarity of an ItemStack by searching for rarity text in lore
         * @param itemStack The ItemStack to analyze
         * @return The detected RarityType, or null if not found
         */
        fun detectRarity(itemStack: ItemStack): RarityType? {
            val lore = TooltipUtils.getLoreAsStrings(itemStack)

            for (line in lore) {
                for (rarity in entries) {
                    if (line.contains(rarity.formattedText)) {
                        return rarity
                    }
                }
            }
            return null
        }
    }
}