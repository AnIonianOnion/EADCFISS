package com.anionianonion.elementalattackdamagecompat.pseudo_enchants;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GemBuffRegistry {

    private static final List<ApotheosisGemEnchantmentBuff> registry = new ArrayList<>();

    public static List<ApotheosisGemEnchantmentBuff> getRegistry() {
        return registry;
    }

    public static void registerNewApotheosisGemEnchantmentBuff(String gemNamespaceAndPath, LootCategory lootCategory, Enchantment enchantment, Function<String, Short> levelsToBuffFunctionBasedOnRarity) {
        registry.add(ApotheosisGemEnchantmentBuff.create(gemNamespaceAndPath, lootCategory, enchantment, levelsToBuffFunctionBasedOnRarity));
    }

    public static List<ApotheosisGemEnchantmentBuff> filterByEnchantment(Enchantment enchantment) {
        return registry.stream().filter(record -> record.enchantmentToBuffLevel() == enchantment).toList();
    }

    public static List<ApotheosisGemEnchantmentBuff> filterByLootCategory(LootCategory lootCategory) {
        return registry.stream().filter(record -> record.lootCategory() == lootCategory).toList();
    }

    public static List<ApotheosisGemEnchantmentBuff> filterByEnchantmentAndLootCategory(Enchantment enchantment, LootCategory lootCategory) {
        return registry.stream()
                .filter(record -> record.enchantmentToBuffLevel() == enchantment && record.lootCategory() == lootCategory)
                .toList();
    }
}
