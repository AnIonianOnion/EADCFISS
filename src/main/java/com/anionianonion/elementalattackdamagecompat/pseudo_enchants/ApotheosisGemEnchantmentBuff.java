package com.anionianonion.elementalattackdamagecompat.pseudo_enchants;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.function.Function;

public record ApotheosisGemEnchantmentBuff(int id, String gemId, LootCategory lootCategory, Enchantment enchantmentToBuffLevel, Function<String, Short> levelsToBuffFunctionBasedOnRarity) {

    public ApotheosisGemEnchantmentBuff {
        // Enforce that only create() may call this
        if (!ConstructionGuard.ALLOW) {
            throw new IllegalStateException("Use ApotheosisGemEnchantmentBuff.create(), not the constructor.");
        }
    }

    public static ApotheosisGemEnchantmentBuff create(
            String gemNamespaceAndPath,
            LootCategory lootCategory,
            Enchantment ench,
            Function<String, Short> levelsToBuffFunctionBasedOnRarity
    ) {
        try {
            ConstructionGuard.ALLOW = true;
            return new ApotheosisGemEnchantmentBuff(
                    GemBuffIdAllocator.nextId(),
                    gemNamespaceAndPath,
                    lootCategory,
                    ench,
                    levelsToBuffFunctionBasedOnRarity
            );
        }
        finally {
            ConstructionGuard.ALLOW = false;
        }

    }

    private static class ConstructionGuard {
        private static boolean ALLOW = false;
    }
}
