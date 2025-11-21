package com.anionianonion.elementalattackdamagecompat;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class CritModifierHelpers {


    protected static void addStackingGlobalCritModifiers(ItemStack itemStack, EquipmentSlot slot,
                                                       List<AttributeModifier> attackCritChanceMods, List<AttributeModifier> attackCritDamageMods,
                                                       Attribute spellCritChance, Attribute spellCritDamage) {

        //add one spell crit modifier for each attack crit modifier
        int cc_index = 0;
        for (var attackMod : attackCritChanceMods) {
            itemStack.addAttributeModifier(spellCritChance,
                    new AttributeModifier(getStackingGlobalCritUUID(itemStack, "global_crit_chance", cc_index),
                            "Global Crit Chance", attackMod.getAmount(), attackMod.getOperation()), slot);
            cc_index++;
        }

        int cd_index = 0;
        for (var attackMod : attackCritDamageMods) {
            itemStack.addAttributeModifier(spellCritDamage,
                    new AttributeModifier(getStackingGlobalCritUUID(itemStack, "global_crit_damage", cd_index),
                            "Global Crit Damage", attackMod.getAmount(), attackMod.getOperation()), slot);
            cd_index++;
        }
    }

    //thanks DeepWiki
    // Generate unique UUID per modifier index
    private static UUID getStackingGlobalCritUUID(ItemStack stack, String suffix, int index) {
        return UUID.nameUUIDFromBytes(
                (stack.getItem().toString() + "_" + suffix + "_" + index).getBytes()
        );
    }

    protected static void removeGlobalCritModifiers(Multimap<Attribute, AttributeModifier> attributeModifiers,
                                                  List<AttributeModifier> spellCritChanceMods, List<AttributeModifier> spellCritDamageMods,
                                                  Attribute spellCritChance, Attribute spellCritDamage) {
        for (var mod : spellCritChanceMods) {
            attributeModifiers.remove(spellCritChance, mod);
        }

        for (var mod : spellCritDamageMods) {
            attributeModifiers.remove(spellCritDamage, mod);
        }
    }
}
