package com.anionianonion.elementalattackdamagecompat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeHelpers {

    public static HashMap<String, Float> getAllElementalData(LivingEntity livingAttacker, LivingEntity livingDefender, boolean isSpell, Map.Entry<String, Float> otherDamage) {

        HashMap<String, Float> baseDamage = getBaseElementalDamagesData(livingAttacker, isSpell, otherDamage);
        HashMap<String, Float> elementalIncreasesAndDecreases = getElementalIncreasesAndDecreasesData(livingAttacker, isSpell);
        HashMap<String, Float> elementalMoreAndLessModifiers = getElementalMoreAndLessModifiersData(livingAttacker, isSpell);
        HashMap<String, Float> enemyElementalResistances = getElementalResistances(livingDefender);

        HashMap<String, Float> result = new HashMap<>(baseDamage);
        /*
        for(var entryB : mapB.entrySet()) {
            mapA.compute(entryB.getKey(), (keyA, valueA) -> {
                var num2 = mapB.get(key);
                return valueA plusMinusTimesDivide num2;
            })
        }
         */


        for(Map.Entry<String, Float> entry : elementalIncreasesAndDecreases.entrySet()) {
            result.compute(entry.getKey(), (key, value) -> {
                Float increaseOrDecreaseEffectiveMultiplier = elementalIncreasesAndDecreases.get(key);
                return value * increaseOrDecreaseEffectiveMultiplier;
            });
        }

        for(Map.Entry<String, Float> entry : elementalMoreAndLessModifiers.entrySet()) {
            result.compute(entry.getKey(), (key, value) -> {
                Float moreOrLessEffectiveMultiplier = elementalMoreAndLessModifiers.get(key);
                return value * moreOrLessEffectiveMultiplier;
            });
        }

        for(Map.Entry<String, Float> entry : enemyElementalResistances.entrySet()) {
            result.compute(entry.getKey(), (key, value) -> {
                Float postResistanceDamageMultiplier = enemyElementalResistances.get(key);
                return value * postResistanceDamageMultiplier;
            });
        }

        return result;
    }
    public static HashMap<String, Float> getBaseElementalDamagesData(LivingEntity livingAttacker, boolean isSpell, Map.Entry<String, Float> otherDamage) {

        HashMap<String, Float> baseElementalData = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.ADDITION);
        if(ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES.contains(otherDamage.getKey())) {
            baseElementalData.put(
                    otherDamage.getKey(),
                    baseElementalData.get(otherDamage.getKey()) + otherDamage.getValue()
            );
        }
        else {
            baseElementalData.put(otherDamage.getKey(), otherDamage.getValue());
        }
        return baseElementalData;
    }
    public static List<String> getSchoolType(int type) {
        return switch (type) {
            case 1 -> Config.type1schools;
            case 2 -> Config.type2schools;
            case 3 -> Config.type3schools;
            case 4 -> Config.type4schools;
            case 5 -> Config.type5schools;
            default -> throw new IllegalArgumentException(
                    String.format("There are only %s parent types of schools. You tried getting the overarching type for %s, which doesn't exist in getSchoolTypes(), or is out of bounds.",
                            ModAttributes.numTypes, type));
        };
    }
    public static HashMap<String, Float> getElementalIncreasesAndDecreasesData(LivingEntity livingAttacker, boolean isSpell) {
        HashMap<String, Float> elementalIncreasesAndDecreasesData = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.MULTIPLY_BASE);

        if(!isSpell) {
            float netIncreasePhysicalMultiplier = 1 + getNetIncrease(livingAttacker, Attributes.ATTACK_DAMAGE);
            elementalIncreasesAndDecreasesData.put("physical", netIncreasePhysicalMultiplier);
        }

        /*
        float type1SchoolNetIncrease = getNetIncrease(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_1_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type1schools) {
            if(elementalIncreasesAndDecreasesData.containsKey(spellSchool)) {
                elementalIncreasesAndDecreasesData.replace(
                        spellSchool,
                        elementalIncreasesAndDecreasesData.get(spellSchool) + type1SchoolNetIncrease);
            }
        }

        float type2SchoolNetIncrease = getNetIncrease(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_2_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type2schools) {
            if(elementalIncreasesAndDecreasesData.containsKey(spellSchool)) {
                elementalIncreasesAndDecreasesData.replace(
                        spellSchool,
                        elementalIncreasesAndDecreasesData.get(spellSchool) + type2SchoolNetIncrease);
            }
        }

        float type3SchoolNetIncrease = getNetIncrease(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_3_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type3schools) {
            if(elementalIncreasesAndDecreasesData.containsKey(spellSchool)) {
                elementalIncreasesAndDecreasesData.replace(
                        spellSchool,
                        elementalIncreasesAndDecreasesData.get(spellSchool) + type3SchoolNetIncrease);
            }
        }
        */

        for(int i = 1; i <= ModAttributes.numTypes; i++) {
            float type_i_SchoolNetIncrease = getNetIncrease(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, i)));
            for(String spellSchool : getSchoolType(i)) {
                if(elementalIncreasesAndDecreasesData.containsKey(spellSchool)) {
                    elementalIncreasesAndDecreasesData.replace(
                            spellSchool,
                            elementalIncreasesAndDecreasesData.get(spellSchool) + type_i_SchoolNetIncrease);
                }
            }
        }

        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute spellOrAttackAttribute = ModAttributes.getAttribute(String.format("%s:%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, spellOrAttack));
        float sum = getNetIncrease(livingAttacker, spellOrAttackAttribute);
        elementalIncreasesAndDecreasesData.replaceAll((k, v) -> v + sum);

        return elementalIncreasesAndDecreasesData;
    }

    public static HashMap<String, Float> getElementalMoreAndLessModifiersData(LivingEntity livingAttacker, boolean isSpell) {
        HashMap<String, Float> elementalMoreAndLessModifiersData = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.MULTIPLY_TOTAL);

        if(!isSpell) {
            float effectiveMorePhysMultiplier = 1 + getEffectiveMore(livingAttacker, Attributes.ATTACK_DAMAGE);
            elementalMoreAndLessModifiersData.put("physical", effectiveMorePhysMultiplier);
        }

        /*
        float type1SchoolEffectiveMoreMultiplier = 1 + getEffectiveMore(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_1_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type1schools) {
            if(elementalMoreAndLessModifiersData.containsKey(spellSchool)) {
                elementalMoreAndLessModifiersData.replace(
                        spellSchool,
                        elementalMoreAndLessModifiersData.get(spellSchool) * type1SchoolEffectiveMoreMultiplier);
            }
        }

        float type2SchoolEffectiveMoreMultiplier = 1 + getEffectiveMore(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_2_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type2schools) {
            if(elementalMoreAndLessModifiersData.containsKey(spellSchool)) {
                elementalMoreAndLessModifiersData.replace(
                        spellSchool,
                        elementalMoreAndLessModifiersData.get(spellSchool) * type2SchoolEffectiveMoreMultiplier);
            }
        }

        float type3SchoolEffectiveMoreMultiplier = 1 + getEffectiveMore(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_3_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID)));
        for(String spellSchool : Config.type3schools) {
            if(elementalMoreAndLessModifiersData.containsKey(spellSchool)) {
                elementalMoreAndLessModifiersData.replace(
                        spellSchool,
                        elementalMoreAndLessModifiersData.get(spellSchool) * type3SchoolEffectiveMoreMultiplier);
            }
        }
        */

        for(int i = 1; i <= ModAttributes.numTypes; i++) {
            float type_i_SchoolEffectiveMoreMultiplier = 1 + getEffectiveMore(livingAttacker, ModAttributes.getAttribute(String.format("%s:type_%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, i)));
            for(String spellSchool : getSchoolType(i)) {
                if(elementalMoreAndLessModifiersData.containsKey(spellSchool)) {
                    elementalMoreAndLessModifiersData.replace(
                            spellSchool,
                            elementalMoreAndLessModifiersData.get(spellSchool) * type_i_SchoolEffectiveMoreMultiplier);
                }
            }
        }

        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute spellOrAttackAttribute = ModAttributes.getAttribute(String.format("%s:%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, spellOrAttack));
        float product = getEffectiveMore(livingAttacker, spellOrAttackAttribute);
        elementalMoreAndLessModifiersData.replaceAll((k, v) -> v * (1 + product));

        return elementalMoreAndLessModifiersData;
    }
    private static HashMap<String, Float> getElementalResistances(LivingEntity livingDefender) {
        HashMap<String, Float> elementalResistanceData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            //Enemy resistances
            Float elementalResistance = ModAttributes.getAttributeValue(livingDefender, String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName));
            if(elementalResistance == null && ModAttributes.customSchoolToResistAttributeKey.containsKey(elementalAttributeName)) {
                elementalResistance = ModAttributes.getAttributeValue(livingDefender, elementalAttributeName);
            }

            if(elementalResistance == null) elementalResistance = 1f; //by default, what elemental resistances are.

            elementalResistanceData.put(elementalAttributeName, elementalResistance);
        }

        return elementalResistanceData;
    }

    private static List<AttributeModifier> filterSpecificElementalAttributeModifiersByOperation(LivingEntity livingAttacker, String elementalAttributeName, boolean isSpell, AttributeModifier.Operation operation) {
        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute elementalDamageAttribute = ModAttributes.getAttribute(String.format("%s:%s_%s_damage", ElementalAttackDamageCompatMod.MOD_ID, elementalAttributeName, spellOrAttack));

        if(elementalDamageAttribute != null && livingAttacker.getAttribute(elementalDamageAttribute) != null) {
            return livingAttacker.getAttribute(elementalDamageAttribute).getModifiers()
                    .stream()
                    .filter(attribute -> attribute.getOperation() == operation)
                    .toList();
        }
        return null;
    }

    //depending on operation: gets hashmap detailing base added damage, increased/decreased damage, or more/less damage.
    private static HashMap<String, Float> getElementalDataForGivenOperation(LivingEntity livingAttacker, boolean isSpell, AttributeModifier.Operation operation) {

        HashMap<String, Float> elementalData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            List<AttributeModifier> attributeModifiers = filterSpecificElementalAttributeModifiersByOperation(livingAttacker, elementalAttributeName, isSpell, operation);

            if(attributeModifiers != null) {

                if(operation == AttributeModifier.Operation.ADDITION || operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                    float sum = operation == AttributeModifier.Operation.MULTIPLY_BASE ? 1 : 0;
                    for (var attributeModifier : attributeModifiers) {
                        sum += (float) attributeModifier.getAmount();
                    }
                    elementalData.put(elementalAttributeName, sum);
                }
                else {
                    float product = 1;
                    for (var attributeModifier : attributeModifiers) {
                        product *= (float) (1 + attributeModifier.getAmount());
                    }
                    elementalData.put(elementalAttributeName, product);
                }
            }
            else {
                switch (operation) {
                    case ADDITION:
                        elementalData.put(elementalAttributeName, 0f);
                        break;
                    case MULTIPLY_BASE:
                    case MULTIPLY_TOTAL:
                        elementalData.put(elementalAttributeName, 1f);
                }
            }
        }
        return elementalData;
    }
    public static HashMap<String, Float> getCritData(LivingEntity livingAttackerOrCaster, boolean isSpell) {

        HashMap<String, Float> critData = new HashMap<>();
        Attribute critChance, critDamage, secondaryCritChance, secondaryCritDamage;
        Float critChanceBaseAmount, critDamageBaseAmount, secondaryCritChanceBaseAmount, secondaryCritDamageBaseAmount;
        float critChanceNetIncreaseAmount, critDamageNetIncreaseAmount;
        float critChanceEffectiveMoreAmount, critDamageEffectiveMoreAmount;

        //sets the sources of critChance and critDamage, and secondary critChance and critDamage.
        if(isSpell) {
            critChance = ModAttributes.getAttribute(Config.spellCritChanceAttributeId);
            critDamage = ModAttributes.getAttribute(Config.spellCritDamageAttributeId);

            if(Config.applyAttackCritAttributesGlobally) {
                secondaryCritChance = ModAttributes.getAttribute(Config.attackCritChanceAttributeId);
                secondaryCritDamage = ModAttributes.getAttribute(Config.attackCritDamageAttributeId);
            }
            else {
                secondaryCritChance = ModAttributes.getAttribute(Config.globalCritChanceAttributeId);
                secondaryCritDamage = ModAttributes.getAttribute(Config.globalCritDamageAttributeId);
            }
        }
        else {
            critChance = ModAttributes.getAttribute(Config.attackCritChanceAttributeId);
            critDamage = ModAttributes.getAttribute(Config.attackCritDamageAttributeId);

            if(!Config.applyAttackCritAttributesGlobally) {
                secondaryCritChance = ModAttributes.getAttribute(Config.globalCritChanceAttributeId);
                secondaryCritDamage = ModAttributes.getAttribute(Config.globalCritDamageAttributeId);
            }
            else {
                secondaryCritChance = null;
                secondaryCritDamage = null;
            }
        }
        critChanceBaseAmount = getBaseTotal(livingAttackerOrCaster, critChance);
        critDamageBaseAmount = getBaseTotal(livingAttackerOrCaster, critDamage);

        secondaryCritChanceBaseAmount = getBaseTotal(livingAttackerOrCaster, secondaryCritChance);
        secondaryCritDamageBaseAmount = getBaseTotal(livingAttackerOrCaster, secondaryCritDamage);

        critChanceBaseAmount += secondaryCritChanceBaseAmount + (float) Config.modCompatCritChanceOffset;
        critDamageBaseAmount += secondaryCritDamageBaseAmount + (float) Config.modCompatCritDamageOffset;

        critChanceNetIncreaseAmount = getNetIncrease(livingAttackerOrCaster, critChance) + getNetIncrease(livingAttackerOrCaster, secondaryCritChance);
        critDamageNetIncreaseAmount = getNetIncrease(livingAttackerOrCaster, critDamage) + getNetIncrease(livingAttackerOrCaster, secondaryCritDamage);

        critChanceEffectiveMoreAmount = getEffectiveMore(livingAttackerOrCaster, critChance) + getEffectiveMore(livingAttackerOrCaster, secondaryCritChance);
        critDamageEffectiveMoreAmount = getEffectiveMore(livingAttackerOrCaster, critDamage) + getEffectiveMore(livingAttackerOrCaster, secondaryCritDamage);

        critData.put("crit_chance", critChanceBaseAmount * (1 + critChanceNetIncreaseAmount) * (1 + critChanceEffectiveMoreAmount));
        critData.put("crit_damage", critDamageBaseAmount * (1 + critDamageNetIncreaseAmount) * (1 + critDamageEffectiveMoreAmount));

        return critData;
    }

    //other methods
    public static Float getBaseTotal(LivingEntity livingEntity, Attribute attribute) {
        float base = 0;
        if(attribute != null && livingEntity.getAttribute(attribute) != null) {
            base = (float) livingEntity.getAttribute(attribute).getBaseValue();
            List<AttributeModifier> addedOrSubtractedModifiers = filterAttributeModifiersByOperation(livingEntity, attribute, AttributeModifier.Operation.ADDITION);
            for(AttributeModifier attributeModifier : addedOrSubtractedModifiers) {
                base += (float) attributeModifier.getAmount();
            }
        }
        return base;
    }
    public static Float getNetIncrease(LivingEntity livingEntity, Attribute attribute) {
        float netIncrease = 0;
        if(attribute != null && livingEntity.getAttribute(attribute) != null) {
            List<AttributeModifier> increaseOrDecreaseDamageModifiers = filterAttributeModifiersByOperation(livingEntity, attribute, AttributeModifier.Operation.MULTIPLY_BASE);
            for(AttributeModifier attributeModifier : increaseOrDecreaseDamageModifiers) {
                netIncrease += (float) attributeModifier.getAmount();
            }
        }
        return netIncrease;
    }
    public static Float getEffectiveMore(LivingEntity livingEntity, Attribute attribute) {
        float effectiveMore = 1;
        if(attribute != null && livingEntity.getAttribute(attribute) != null) {
            List<AttributeModifier> increaseOrDecreaseDamageModifiers = filterAttributeModifiersByOperation(livingEntity, attribute, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for(AttributeModifier attributeModifier : increaseOrDecreaseDamageModifiers) {
                effectiveMore *= 1 + (float) attributeModifier.getAmount();
            }
        }
        return effectiveMore - 1;
    }
    public static List<AttributeModifier> filterAttributeModifiersByOperation(LivingEntity livingEntity, Attribute attribute, AttributeModifier.Operation operation) {
        return livingEntity.getAttribute(attribute)
                .getModifiers()
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == operation)
                .toList();
    }
}
