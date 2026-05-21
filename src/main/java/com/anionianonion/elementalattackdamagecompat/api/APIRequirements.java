package com.anionianonion.elementalattackdamagecompat.api;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.PseudoEnchantmentHandler;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.function.Function;

interface APIRequirements
{
    void createElement(String elementName);
    void removeElement(String elementName);

    void addCustomResistAttributeForElement(String elementName, String resistKey, int offset);

    void addDefaultAilmentToElement(String element, String ailment);

    void removeAilmentFromElement(String element, String ailment);

    void createDamagingAilment(String ailmentName);
    void createNonDamagingAilment(String ailmentName);

    void addDamagingAilmentEffect(String ailment, AilmentEffect effect);

    void addNonDamagingAilmentEffect(String ailment, AilmentEffect effect);

    void removeAilment(String ailmentName);

    void attributizeEnchantment(Enchantment enchantment, String key, List<PseudoEnchantmentHandler.PseudoAttribute> pseudoAttributes);

    void registerNewApotheosisGemEnchantmentBuff(String gemNamespaceAndPath, LootCategory lootCategory, Enchantment enchantment, Function<String, Short> levelsToBuffFunctionBasedOnRarity);
}
