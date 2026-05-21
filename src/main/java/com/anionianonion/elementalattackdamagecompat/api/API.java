package com.anionianonion.elementalattackdamagecompat.api;

import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry.AilmentCategory;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentsRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.GemBuffRegistry;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.PseudoEnchantmentHandler;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.function.Function;

public class API implements APIRequirements {

    // ---------------------------
    // ELEMENT MANAGEMENT
    // ---------------------------

    //in the current system, each element is also a school, which may be problematic.
    //But I don't think it's possible to map each school to one element without a major overhaul and guarding for edge cases.
    //Ex: no physical element yet, though the default attack damage is treated as such.
    // But that doesn't apply bleeding, since it's not technically an element.
    //If we wanted to create a physical element, it throws an error. Unless we create a separation between school and element.

    //This ailment system started just because I thought it would be cool to scale DoT damage from elements like in PoE, which isn't possible in Vanilla Minecraft.
    @Override
    public void createElement(String element) {
        AilmentsRegistry.createElement(element);
    }

    @Override
    public void removeElement(String elementName) {
        AilmentsRegistry.removeElement(elementName);
    }

    /**
    For non-ISS elements, you may apply resistance from this attribute for that element.

     This mod, when calculating damage, assumes (because of Iron's Spells and Spellbooks' normalized resist attributes) that the specific elemental resistance is normalized between 1 and 2, where 1 is 0% resistance to that element, and 2 is 100%.
     Then it renormalizes it to be between 0 and 1, where 0 is 0% resist to that, and 1 is 100%. If your resist attribute is already normalized between 0 and 1, you may add a 1 as the offset here to
     stop double-normalization.
     */
    @Override
    public void addCustomResistAttributeForElement(String elementName, String resistKey, int resistOffset) {
        ModAttributes.customSchoolToResistAttributeKey.putIfAbsent(elementName, resistKey);
        ModAttributes.resistAttributeKeyToResistOffset.putIfAbsent(resistKey, resistOffset);
    }

    @Override
    public void addDefaultAilmentToElement(String element, String ailment) {
        AilmentsRegistry.addDefaultAilmentToElement(element, ailment);
    }

    @Override
    public void removeAilmentFromElement(String element, String ailment) {
        AilmentsRegistry.removeAilmentFromElement(element, ailment);
    }


    // ---------------------------
    // AILMENT REGISTRATION
    // ---------------------------

    @Override
    public void createDamagingAilment(String ailment) {
        // Register ailment with a placeholder effect until user assigns one
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.DAMAGING,
                AilmentEffect.empty() // safe placeholder, never null
        );
    }

    @Override
    public void createNonDamagingAilment(String ailment) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.NON_DAMAGING,
                AilmentEffect.empty()
        );
    }

    @Override
    public void addDamagingAilmentEffect(String ailment, AilmentEffect effect) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.DAMAGING,
                effect
        );
    }

    @Override
    public void addNonDamagingAilmentEffect(String ailment, AilmentEffect effect) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.NON_DAMAGING,
                effect
        );
    }


    // ---------------------------
    // AILMENT REMOVAL
    // ---------------------------

    @Override
    public void removeAilment(String ailment) {
        AilmentEffectRegistry.removeAilment(ailment);
    }


    // ---------------------------
    // DEBUG / MAINTENANCE
    // ---------------------------

    public void printAilmentRegistry() {
        AilmentEffectRegistry.printDebug();
    }

    @Override
    public void attributizeEnchantment(Enchantment enchantment, String nameYourEnchantmentKey, List<PseudoEnchantmentHandler.PseudoAttribute> pseudoAttributes) {
        PseudoEnchantmentHandler.addEnchantmentAndKey(enchantment, nameYourEnchantmentKey);
        PseudoEnchantmentHandler.addKeyAndPseudoAttributes(nameYourEnchantmentKey, pseudoAttributes);
    }

    @Override
    public void registerNewApotheosisGemEnchantmentBuff(String gemNamespaceAndPath, LootCategory lootCategory, Enchantment enchantment, Function<String, Short> levelsToBuffFunctionBasedOnRarity) {
        GemBuffRegistry.registerNewApotheosisGemEnchantmentBuff(gemNamespaceAndPath, lootCategory, enchantment, levelsToBuffFunctionBasedOnRarity);
    }
}
