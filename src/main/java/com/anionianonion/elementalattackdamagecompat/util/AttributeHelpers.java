package com.anionianonion.elementalattackdamagecompat.util;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentDataHelper;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public class AttributeHelpers {

    //--------------------------------------------------------ATTRIBUTE GETTERS--------------------------------------------------------
    //ties everything together.
    public static HashMap<String, Float> getBasicElementalData(LivingEntity livingAttacker, LivingEntity livingDefender, boolean isSpell, Map.Entry<String, Float> otherDamageData) {

        HashMap<String, Float> baseElementalDamageData = getBaseElementalDamageData(otherDamageData);
        HashMap<String, Float> elementalAddedDamageData = getAddedElementalDamageData(livingAttacker, isSpell);
        HashMap<String, Float> elementalIncreasesAndDecreases = getElementalIncreasesAndDecreasesData(livingAttacker, isSpell);
        HashMap<String, Float> elementalMoreAndLessModifiers = getElementalMoreAndLessModifiersData(livingAttacker, isSpell);

        HashMap<String, Float> result = new HashMap<>(baseElementalDamageData);

        float effectiveAddedDamageMultiplier = 1.0f;
        for(Map.Entry<String, Float> entry : elementalAddedDamageData.entrySet()) {
            result.compute(entry.getKey(), (key, value) -> {
                Float addedBaseDamage = elementalAddedDamageData.get(key);
                return value + addedBaseDamage * effectiveAddedDamageMultiplier;
            });
        }

        addShockEffectToDamageIncreasesAndDecreases(elementalIncreasesAndDecreases, livingAttacker, livingDefender);

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

        if(livingDefender.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            result.put("fire", 0f);
        }

        return result;
    }

    /**
    Creates a Hashmap containing the base damage dealt for every element. otherDamage is used to pass in data dealt by an attack or spell.
     */
    public static HashMap<String, Float> getBaseElementalDamageData(Map.Entry<String, Float> otherDamage) {
        HashMap<String, Float> baseElementalDamageData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            baseElementalDamageData.put(elementalAttributeName, 0f);
        }

        if(ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES.contains(otherDamage.getKey())) {
            baseElementalDamageData.put(
                    otherDamage.getKey(),
                    baseElementalDamageData.get(otherDamage.getKey()) + otherDamage.getValue()
            );
        }
        else {
            baseElementalDamageData.put(otherDamage.getKey(), otherDamage.getValue());
        }

        return baseElementalDamageData;

    }
    public static HashMap<String, Float> getAddedElementalDamageData(LivingEntity livingAttacker, boolean isSpell) {

        return getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.ADDITION);
    }
    public static HashMap<String, Float> getElementalIncreasesAndDecreasesData(LivingEntity livingAttacker, boolean isSpell) {
        HashMap<String, Float> elementalIncreasesAndDecreasesData = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.MULTIPLY_BASE);

        if(!isSpell) {
            float netIncreasePhysicalMultiplier = 1 + getNetIncrease(livingAttacker, Attributes.ATTACK_DAMAGE);
            elementalIncreasesAndDecreasesData.put("physical", netIncreasePhysicalMultiplier);
        }

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
    public static HashMap<String, Float> getElementalResistances(LivingEntity livingDefender) {
        HashMap<String, Float> elementalResistanceData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            //Enemy resistances
            //this line deals with resistances from the main mod ISS
            //todo: this returns the final value of elemental resistance, which is already affected by its own increases/decreases & more.
            //Float elementalResistance = ModAttributes.getAttributeValue(livingDefender, String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName));

            Attribute specificEleRes = ModAttributes.getAttribute(String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName));
            Attribute allAroundEleRes = ModAttributes.getAttribute(String.format("%s:all_elemental_resistances", ElementalAttackDamageCompatMod.MOD_ID));

            Float baseSpecificEleResAmount;
            if(specificEleRes != null) baseSpecificEleResAmount = getBaseTotal(livingDefender, specificEleRes) - 1;
            else {
                var attributeKey = ModAttributes.customSchoolToResistAttributeKey.get(elementalAttributeName);
                specificEleRes = ModAttributes.getAttribute(attributeKey);
                Integer offset = ModAttributes.resistAttributeKeyToResistOffset.get(attributeKey);
                if(specificEleRes != null) {
                    baseSpecificEleResAmount = getBaseTotal(livingDefender, specificEleRes) - 1;
                    if(offset != null) baseSpecificEleResAmount += offset;
                }
                else {
                    baseSpecificEleResAmount = 0f;
                }
            }

            Float baseAllAroundEleResAmount = getBaseTotal(livingDefender, allAroundEleRes);


            float totalBaseResistance = baseSpecificEleResAmount + baseAllAroundEleResAmount;
            float netIncreasedRes = getNetIncrease(livingDefender, allAroundEleRes) + getNetIncrease(livingDefender, specificEleRes);
            float effectiveMoreRes = (1 + getEffectiveMore(livingDefender, allAroundEleRes)) * (1 + getEffectiveMore(livingDefender, specificEleRes));

            float elementalResistance = totalBaseResistance * (1 + netIncreasedRes) * effectiveMoreRes;

            elementalResistanceData.put(elementalAttributeName, elementalResistance);
        }

        return elementalResistanceData;
    }
    /**
    This data's crit chance takes into account attacker's crit chance values, as well as defender's brittle ailmentEffect value.
     Values may be accessed by doing .get("crit_chance"); or .get("crit_damage");
     */
    public static HashMap<String, Float> getCritData(LivingEntity livingAttackerOrCaster, LivingEntity livingDefender, boolean isSpell) {

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
        //not spell, so attack
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

        AilmentInstance instance = AilmentDataHelper.getAilment(livingDefender, "brittle");
        float brittle = instance != null ? instance.strongestEffectStrength : 0f;

        critChanceBaseAmount += secondaryCritChanceBaseAmount + (float) Config.modCompatCritChanceOffset + brittle;
        critDamageBaseAmount += secondaryCritDamageBaseAmount + (float) Config.modCompatCritDamageOffset;

        critChanceNetIncreaseAmount = getNetIncrease(livingAttackerOrCaster, critChance) + getNetIncrease(livingAttackerOrCaster, secondaryCritChance);
        critDamageNetIncreaseAmount = getNetIncrease(livingAttackerOrCaster, critDamage) + getNetIncrease(livingAttackerOrCaster, secondaryCritDamage);

        critChanceEffectiveMoreAmount = getEffectiveMore(livingAttackerOrCaster, critChance) + getEffectiveMore(livingAttackerOrCaster, secondaryCritChance);
        critDamageEffectiveMoreAmount = getEffectiveMore(livingAttackerOrCaster, critDamage) + getEffectiveMore(livingAttackerOrCaster, secondaryCritDamage);

        critData.put("crit_chance", critChanceBaseAmount * (1 + critChanceNetIncreaseAmount) * (1 + critChanceEffectiveMoreAmount));
        critData.put("crit_damage", critDamageBaseAmount * (1 + critDamageNetIncreaseAmount) * (1 + critDamageEffectiveMoreAmount));

        return critData;
    }

    public static void addShockEffectToDamageIncreasesAndDecreases(
            HashMap<String, Float> elementalIncreasesAndDecreasesData,
            LivingEntity attacker,
            LivingEntity defender
    ) {
        AilmentInstance shock = AilmentDataHelper.getAilment(defender, "shock");
        AilmentInstance oosShock = AilmentDataHelper.getAilment(defender, "oath_of_spring_shock");

        if(oosShock != null && Config.enableDebugMode) {
            if(attacker instanceof ServerPlayer sp) {
                sp.sendSystemMessage(Component.literal("OoS Shock stacks: " + oosShock.stacks.size()));
            }
            else {
                ElementalAttackDamageCompatMod.LOGGER.info("OoS Shock stacks by " + attacker.getName().getString() + ": " + oosShock.stacks.size());
            }
        }

        float shockEffectValue = shock != null ? shock.strongestEffectStrength : 0f;
        float ooSShockEffectValue = oosShock != null ? oosShock.totalEffectStrength : 0f;

        float max = Math.max(shockEffectValue, ooSShockEffectValue);

        for (String key : elementalIncreasesAndDecreasesData.keySet()) {
            elementalIncreasesAndDecreasesData.merge(key, max, Float::sum);
        }
    }

    public static void multiplyWithEnemyResistances(HashMap<String, Float> attackerData, LivingEntity livingDefender, boolean isSpell) {
        HashMap<String, Float> enemyElementalResistances = getElementalResistances(livingDefender);

        Float genericSpellResistance = ModAttributes.getAttributeValue(livingDefender, "irons_spellbooks:spell_resist");
        float hardcappedResist = 0.90f;
        float hardflooredResist = -2f;

        //todo: fix scorch, and test brittle
        AilmentInstance scorch = AilmentDataHelper.getAilment(livingDefender, "scorch");
        float scorchEffectValue = scorch != null ? scorch.strongestEffectStrength : 0f;

        if(Config.enableDebugMode) {
            boolean isNull = scorch == null;
            ElementalAttackDamageCompatMod.LOGGER.info("instance == null? " + (isNull));
            if(!isNull) {
                //always 0 because Scorch uses Strongest Effect
                //ElementalAttackDamageCompatMod.LOGGER.info("Scorch stacks: " + scorch.stacks.size());
                ElementalAttackDamageCompatMod.LOGGER.info("scorch: " + scorchEffectValue);
                ElementalAttackDamageCompatMod.LOGGER.info("strongestEffectStrength: " + scorch.strongestEffectStrength);
            }
        }

        for(Map.Entry<String, Float> entry : enemyElementalResistances.entrySet()) {
            attackerData.compute(entry.getKey(), (key, value) -> {
                Float resistance = enemyElementalResistances.get(key);

                Float softcappedElementalResist = ModAttributes.getAttributeValue(livingDefender, String.format("%s:%s_max_resistance", ElementalAttackDamageCompatMod.MOD_ID, key));
                if(softcappedElementalResist == null) softcappedElementalResist = 0.5f;

                float hardcappedOrSoftcappedResist = Math.min(softcappedElementalResist, hardcappedResist);

                //our resistance is now normalized on a scale from 0 to 1, where 0 is 0% resistance, and 1 is 100% resist. Negative resists work as expected.
                //if it's spell damage, also add spell resistances, which isn't normalized, but on a scale from 1 to 2, where 1 is 0% spell resist.
                float attackOrSpellResistance = !isSpell ? resistance : resistance + (genericSpellResistance - 1);
                float cappedResistance = Math.min(attackOrSpellResistance, hardcappedOrSoftcappedResist);

                float clampedElementalResistance = Math.max(cappedResistance, hardflooredResist); //because we set the softcap/hardcap (aka. max) and the hardfloor (aka. min), and only want the resist to be between these values

                clampedElementalResistance -= scorchEffectValue;

                //verified formula
                //let's say you deal 100 fire damage, and the enemy has 25% fire resist.
                //then value is 100. flooredResistance is 0.25. So you deal 100 * (1 - .25) = 100 * 0.75 = 75 fire damage.
                return value * (1 - clampedElementalResistance);
            });
        }
    }
    public static void multiplyWithArrowSpeed(HashMap<String, Float> elementalData, Arrow arrow) {
        for(Map.Entry<String, Float> entry : elementalData.entrySet()) {
            elementalData.compute(entry.getKey(), (key, value) ->
                    (float) (value * arrow.getDeltaMovement().length())
            );
        }
    }
    public static void multiplyWithCritDamageForArrowsIfCrit(HashMap<String, Float> elementalData, LivingEntity livingAttacker, LivingEntity livingDefender, Arrow arrow) {
        if(arrow.isCritArrow() && Config.disableVanillaFullyChargedBowCrit) {
            arrow.setCritArrow(rollForIfAttacksOrSpellsCrit(livingAttacker, livingDefender, false));
        }

        if(!arrow.isCritArrow()) return;

        HashMap<String, Float> critData = getCritData(livingAttacker, livingDefender, false);
        float critMultiplier = critData.get("crit_damage");

        multiplyWithConstantMultiplier(elementalData, critMultiplier);
    }
    public static void multiplyWithCritDamageIfCrit(HashMap<String, Float> elementalData, LivingEntity livingAttackerOrCaster, LivingEntity livingDefender, boolean isSpell) {

        boolean isCrit = rollForIfAttacksOrSpellsCrit(livingAttackerOrCaster, livingDefender, isSpell);
        if(!isCrit) return;

        HashMap<String, Float> critData = getCritData(livingAttackerOrCaster, livingDefender, isSpell);
        float critMultiplier = critData.get("crit_damage");

        multiplyWithConstantMultiplier(elementalData, critMultiplier);
    }
    public static void multiplyWithCritDamageIfMeleeCrit(HashMap<String, Float> elementalData, LivingEntity livingAttacker, LivingEntity livingDefender) {
        boolean isFallCrit = !livingAttacker.onGround() && livingAttacker.fallDistance > 0 && !livingAttacker.isInWater() && !livingAttacker.onClimbable() && !livingAttacker.isSprinting() && !livingAttacker.hasEffect(MobEffects.BLINDNESS) && !Config.disableVanillaFallingCrit;
        boolean isCrit = rollForIfAttacksOrSpellsCrit(livingAttacker, livingDefender, false);

        if(!isFallCrit && !isCrit) return;

        HashMap<String, Float> critData = getCritData(livingAttacker, livingDefender, false);
        float critMultiplier = critData.get("crit_damage");

        multiplyWithConstantMultiplier(elementalData, critMultiplier);
    }
    public static void multiplyWithSpellSuppressionIfSuppressed(HashMap<String, Float> elementalData, LivingEntity livingDefender) {

        Float spellSuppressionChance = ModAttributes.getAttributeValue(livingDefender, String.format("%s:spell_suppression_chance", ElementalAttackDamageCompatMod.MOD_ID));
        if(spellSuppressionChance == null) spellSuppressionChance = 0f;
        Float spellSuppressionPrevented = ModAttributes.getAttributeValue(livingDefender, String.format("%s:spell_suppression_prevented", ElementalAttackDamageCompatMod.MOD_ID));
        if(spellSuppressionPrevented == null) spellSuppressionPrevented = 0.5f;

        float roll = (float) Math.random();
        if(spellSuppressionChance < roll) return;

        multiplyWithConstantMultiplier(elementalData, spellSuppressionPrevented);
    }
    public static void applyLessDamageFromPossibleSapEffects(HashMap<String, Float> elementalData, LivingEntity livingAttacker) {
        AilmentInstance instance = AilmentDataHelper.getAilment(livingAttacker, "sap");
        float sap = instance != null ? instance.strongestEffectStrength : 0f;
        float dmgMultiplier = 1 - sap;

        multiplyWithConstantMultiplier(elementalData, dmgMultiplier);
    }
    public static void multiplyWithWeaponReadiness(HashMap<String, Float> elementalData, Player player) {
        float readiness = player.getAttackStrengthScale(0.5f);

        multiplyWithConstantMultiplier(elementalData, readiness);
    }
    public static void applyDamageReductionFromProtection(HashMap<String, Float> elementalData, LivingEntity livingAttacker, LivingEntity livingDefender) {

        int epf = EnchantmentHelper.getDamageProtection(livingDefender.getArmorSlots(), livingAttacker.damageSources().magic());
        if(epf > 0) {
            float reduction = Math.min(epf, 20) / 25f;
            float multiplier = 1 - reduction;
            for(String key : elementalData.keySet()) {
                elementalData.compute(key, (k, v) -> v * multiplier);
            }
        }
    }
    public static void multiplyWithConstantMultiplier(HashMap<String, Float> elementalData, float multiplier) {
        for(String key : elementalData.keySet()) {
            elementalData.compute(key, (k, v) -> v * multiplier);
        }
    }

    public static boolean rollForIfAttacksOrSpellsCrit(LivingEntity livingAttacker, LivingEntity livingDefender, boolean isSpell) {
        Float critChance;
        HashMap<String, Float> critData = AttributeHelpers.getCritData(livingAttacker, livingDefender, isSpell);
        critChance = critData.get("crit_chance");

        float critRoll = (float) Math.random();
        return critChance >= critRoll;
    }

    /**
     *Depending on operation, and if it's a spell or attack: gets hashmap detailing base added damage, increased/decreased damage, or more/less damage for the given attacker.
     * @param livingAttacker LivingEntity to get attributes from.
     * @param isSpell If true, this is considered spell damage. So grab data from spell attributes.
     * @param operation ADDITION, MULTIPLY_BASE, MULTIPLY_TOTAL
     * @return Hashmap containing data for base damage (including added modifiers), increased/decreased damage multiplier, and more/less damage multiplier.
     */
    private static HashMap<String, Float> getElementalDataForGivenOperation(LivingEntity livingAttacker, boolean isSpell, AttributeModifier.Operation operation) {

        HashMap<String, Float> elementalData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            List<AttributeModifier> attributeModifiers = filterSpecificElementalAttributeModifiersByOperation(livingAttacker, elementalAttributeName, isSpell, operation);

            if(attributeModifiers != null) {

                //For ADDITION AND MULTIPLY_BASES, each of these attribute modifier values are intended to be added together in their own set.
                //The initial value of sum is equal to the identity value of multiplication, if operation is MULTIPLY_BASE,
                // and equal to the identity value of addition, if operation is not MULTIPLY_BASE (aka. ADDITION)
                if(operation == AttributeModifier.Operation.ADDITION || operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                    float sum = operation == AttributeModifier.Operation.MULTIPLY_BASE ? 1 : 0;
                    for (var attributeModifier : attributeModifiers) {
                        sum += (float) attributeModifier.getAmount();
                    }
                    elementalData.put(elementalAttributeName, sum);
                }
                else {
                    //The initial value of product is equal to the identity value of multiplication
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
    private static List<AttributeModifier> filterSpecificElementalAttributeModifiersByOperation(LivingEntity livingAttacker, String elementalAttributeName, boolean isSpell, AttributeModifier.Operation operation) {
        String spellOrAttack = isSpell ? "spell" : "attack";
        //modifiers only apply to either attack or spell elemental attributes for this element, but not both.
        Attribute eitherOrlementalDamageAttribute = ModAttributes.getAttribute(String.format("%s:%s_%s_damage", ElementalAttackDamageCompatMod.MOD_ID, elementalAttributeName, spellOrAttack));

        Attribute elementalDamageAttributeForBoth = ModAttributes.getAttribute(String.format("%s:%s_damage", ElementalAttackDamageCompatMod.MOD_ID, elementalAttributeName));

        if(eitherOrlementalDamageAttribute != null && livingAttacker.getAttribute(eitherOrlementalDamageAttribute) != null
                && elementalDamageAttributeForBoth != null && livingAttacker.getAttribute(elementalDamageAttributeForBoth) != null) {

            var a = livingAttacker.getAttribute(eitherOrlementalDamageAttribute).getModifiers()
                    .stream()
                    .filter(attribute -> attribute.getOperation() == operation)
                    .toList();
            var b = livingAttacker.getAttribute(elementalDamageAttributeForBoth).getModifiers()
                    .stream()
                    .filter(attribute -> attribute.getOperation() == operation)
                    .toList();

            var combined = Stream.concat(a.stream(), b.stream()).toList();
            return combined;
        }
        return null;
    }
    //other methods that could work for any attribute, not just elemental ones from this mod.
    /**
     * @param livingEntity The living entity to check from.
     * @param attribute The attribute to check.
     * @return base as a Float (actual base + flat added).
     * This value is intended to be used in the revised attribute modifier formula:
     * getBaseTotal() * (1 + getNetIncrease()) * (1 + getEffectiveMore()).
     */
    public static Float getBaseTotal(LivingEntity livingEntity, Attribute attribute) {
        float base = 0;
        if(attribute != null && livingEntity.getAttribute(attribute) != null) {
            base = (float) Objects.requireNonNull(livingEntity.getAttribute(attribute)).getBaseValue();
            List<AttributeModifier> addedOrSubtractedModifiers = filterAttributeModifiersByOperation(livingEntity, attribute, AttributeModifier.Operation.ADDITION);
            for(AttributeModifier attributeModifier : addedOrSubtractedModifiers) {
                base += (float) attributeModifier.getAmount();
            }
        }
        return base;
    }
    /**
     * @param livingEntity The living entity to check increase/decreases from.
     * @param attribute The attribute to check.
     * @return netIncrease as a Float from a percent notation converted to decimal. -0.1 is a 10% decrease, 0 is the same (no increase or decrease), 0.5 is a 50% increase, 1 is a 100% increase, 2 is a 200% increase and so on.
     */
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
    /**
     * @param livingEntity The living entity to check more/less multipliers from.
     * @param attribute The attribute to check.
     * @return effectiveMore as a Float from a percent notation converted to decimal. -0.1 is 10% less, 0 is the same (no more or less). 0.5 is 50% more, 1 is 100% more, 2 is 200% more and so on.
     */
    public static Float getEffectiveMore(LivingEntity livingEntity, Attribute attribute) {
        float multiplier = 1;
        if(attribute != null && livingEntity.getAttribute(attribute) != null) {
            List<AttributeModifier> moreOrLessDamageModifiers = filterAttributeModifiersByOperation(livingEntity, attribute, AttributeModifier.Operation.MULTIPLY_TOTAL);
            for(AttributeModifier attributeModifier : moreOrLessDamageModifiers) {
                multiplier *= 1 + (float) attributeModifier.getAmount();
            }
        }
        float effectiveMore = multiplier - 1;
        return effectiveMore;
    }

    //gets a list of schools based on the group #
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


    //generic, works with any attribute
    private static List<AttributeModifier> filterAttributeModifiersByOperation(LivingEntity livingEntity, Attribute attribute, AttributeModifier.Operation operation) {
        return livingEntity.getAttribute(attribute)
                .getModifiers()
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == operation)
                .toList();
    }

    public static float getSharedEffectiveMultiplier(LivingEntity livingEntity, List<String> validSharedAttributeKeys) {
        if(validSharedAttributeKeys.isEmpty()) {
            return 1;
        }

        float overallNetIncrease = 0;
        float multiplier = 1; //0% more damage

        for(var key : validSharedAttributeKeys) {

            Attribute attribute = ModAttributes.getAttribute(key);
            float netIncreaseForAttribute = getNetIncrease(livingEntity, attribute);
            float effectiveMoreForAttribute = getEffectiveMore(livingEntity, attribute);

            overallNetIncrease += netIncreaseForAttribute;
            multiplier *= (1 + effectiveMoreForAttribute);
        }
        return (1 + overallNetIncrease) * multiplier;
    }

    public static float getNonDamagingAilmentEffectMultiplier(LivingEntity livingAttackerOrCaster, String ailmentEffectName) {
        return AttributeHelpers.getSharedEffectiveMultiplier(livingAttackerOrCaster,
                List.of(
                        String.format("%s:%s_effect", ElementalAttackDamageCompatMod.MOD_ID, ailmentEffectName),
                        String.format("%s:nondamaging_ailment_effect", ElementalAttackDamageCompatMod.MOD_ID)
                )
        );
    }

    public static float getNonDamagingAilmentDurationMultiplier(LivingEntity livingAttackerOrCaster, String ailmentEffectName) {
        return AttributeHelpers.getSharedEffectiveMultiplier(livingAttackerOrCaster,
                List.of(
                        String.format("%s:%s_duration", ElementalAttackDamageCompatMod.MOD_ID, ailmentEffectName),
                        String.format("%s:nondamaging_ailment_duration", ElementalAttackDamageCompatMod.MOD_ID)
                )
        );
    }

    public static float getDamagingAilmentDurationMultiplier(LivingEntity livingAttackerOrCaster, String ailmentEffectName) {
        return AttributeHelpers.getSharedEffectiveMultiplier(livingAttackerOrCaster,
                List.of(
                        String.format("%s:%s_duration", ElementalAttackDamageCompatMod.MOD_ID, ailmentEffectName),
                        String.format("%s:damaging_ailment_duration", ElementalAttackDamageCompatMod.MOD_ID)
                )
        );
    }
}
