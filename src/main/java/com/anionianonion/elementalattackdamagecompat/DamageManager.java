package com.anionianonion.elementalattackdamagecompat;

import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anionianonion.elementalattackdamagecompat.EventHandler.PROCESSING_CUSTOM_DAMAGE;

public class DamageManager {

    public static boolean hasFailedInitialChecks(DamageSource damageSource) {
        if(ElementalAttackDamageCompatMod.IS_RANDOM_DAMAGE_MOD_ENABLED) return true; //don't calculate damage if the user has this mod, as the mod does this step instead.
        if(PROCESSING_CUSTOM_DAMAGE.get()) {
            System.out.println("Skipping: Processing custom damage");
            return true; //prevents recursion with custom sweeping attack after adding elemental damage to it during calculation
        }

        //if(damageSource.isIndirect()) return; //only affects arrows and not sweeping
        var attacker = damageSource.getEntity();
        if(!(attacker instanceof LivingEntity)) return true;

        var directEntity = damageSource.getDirectEntity();
        return directEntity instanceof AbstractMagicProjectile; //simplified from an if statement, because it is the last check, and we can directly return the value as a result.
    }
    public static List<LivingEntity> getNearbyEnemies(Player player, LivingEntity attackedEntity) {

        //it is assumed in our EventHandler that we can sweep
        ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());

        AABB sweepingEdgeHitbox = itemStack.getSweepHitBox(player, attackedEntity);
        Level level = player.level();
        List<LivingEntity> entitiesWithinHitbox = level.getEntitiesOfClass(LivingEntity.class, sweepingEdgeHitbox);
        var enemies = entitiesWithinHitbox.stream()
                .filter(entity -> entity != player)
                .filter(entity -> entity != attackedEntity)
                .filter(entity -> !entity.isAlliedTo(player))
                .filter(entity -> entity.distanceToSqr(player) < 9.0)
                //todo: also filter enemies that can't be hurt
                .toList();

        return enemies;

    }
    public static int getSweepingLevelOfPlayerWeapon(Player player) {
        int sweepingLevel = 0;
        ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());
        if(itemStack.hasTag() && itemStack.getTag().contains("Enchantments")) {

            ListTag enchantments = itemStack.getTag().getList("Enchantments", ListTag.TAG_COMPOUND);

            for(int i = 0; i < enchantments.size(); i++) {

                var enchantment = enchantments.getCompound(i);
                var enchantmentName = enchantment.getString("Name");

                if(enchantmentName.equals("Sweeping Edge")) {
                    sweepingLevel = enchantment.getInt("Lvl");
                    break;
                }
            }
        }
        return sweepingLevel;
    }
    public static void performSweepingAttack(int sweepingLevel, float critAdjustedDamage, DamageSource damageSource, List<LivingEntity> nearbyEnemies, LivingHurtEvent e) {
        e.setAmount(Math.round(critAdjustedDamage)); //main target

        //MC Sweeping Edge Damage formula
        float sweepingDamage = 1 + critAdjustedDamage * ((float) sweepingLevel / (sweepingLevel + 1));
        int roundedSweepingDamage = Math.round(sweepingDamage);

        try {
            PROCESSING_CUSTOM_DAMAGE.set(true);
            //attackedEntity.hurt(damageSource, roundedTotalDamage);
            nearbyEnemies.forEach(entity -> entity.hurt(damageSource, roundedSweepingDamage));
        } finally {
            PROCESSING_CUSTOM_DAMAGE.set(false);
        }
    }

    public static void manageArrowShot(LivingEntity livingAttacker, Arrow arrow,LivingHurtEvent e) {
        LivingEntity livingDefender = e.getEntity();
        float minecraftArrowBaseDamage = (float) arrow.getBaseDamage();
        float newBaseFlatDamage = sumOfDamages(getAllElementalData(livingAttacker, livingDefender, false, Map.entry("physical", minecraftArrowBaseDamage)));
        float flightSpeededAdjustedDamage = (float) (newBaseFlatDamage * arrow.getDeltaMovement().length()); //speed
        float postCritAdjustedDamage = DamageManager.calculateCritArrow(arrow, livingAttacker, flightSpeededAdjustedDamage);

        int roundedDamage = Math.round(postCritAdjustedDamage);
        e.setAmount(roundedDamage);
    }
    public static void manageMeleeAndOtherProjectiles(LivingEntity livingAttacker, Entity directEntity, DamageSource damageSource, LivingHurtEvent e) {
        float baseDamage = e.getAmount();
        LivingEntity livingDefender = e.getEntity();

        float expectedBaseDamage = (float) livingAttacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean vanillaCritApplied = baseDamage > expectedBaseDamage;

        float substitute = baseDamage;
        if(vanillaCritApplied) substitute = baseDamage / 1.5f;

        float newBaseFlatDamage = sumOfDamages(getAllElementalData(livingAttacker, livingDefender, false, Map.entry("physical", substitute)));


        //----MELEE---- ONLY FOR PLAYERS
        if(livingAttacker instanceof Player player && directEntity == player) {

            float critAdjustedDamage = DamageManager.calculateMeleeCrit(player, newBaseFlatDamage);
            ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());

            if(itemStack.canPerformAction(ToolActions.SWORD_SWEEP)) {
                List<LivingEntity> nearbyEnemies = DamageManager.getNearbyEnemies(player, livingDefender);
                int sweepingLevel = DamageManager.getSweepingLevelOfPlayerWeapon(player);
                performSweepingAttack(sweepingLevel, critAdjustedDamage, damageSource, nearbyEnemies, e);
            }
            else {
                int roundedDamage = Math.round(critAdjustedDamage);
                e.setAmount(roundedDamage);
            }
        }
        //---RANGED---: OTHER PROJECTILES (PLAYERS AND NON-PLAYERS)
        //and ---MELEE---(NON-PLAYERS)
        else {
            float critAdjustedDamage;
            //MELEE (NON-PLAYERS)
            if(directEntity == livingAttacker) {
                critAdjustedDamage = DamageManager.calculateMeleeCrit(livingAttacker, newBaseFlatDamage);
            }
            //RANGED: OTHER PROJECTILES
            else {
                critAdjustedDamage = DamageManager.simpleCritRoll(livingAttacker, false, newBaseFlatDamage);
            }
            int roundedDamage = Math.round(critAdjustedDamage);
            e.setAmount(roundedDamage);
        }
    }

    public static float calculateCritArrow(Arrow arrow, LivingEntity livingAttacker, float preCritDamage) {
        //vanilla MC assumes that a fully charged bow or crossbow will automatically crit.
        if(arrow.isCritArrow() && Config.disableVanillaFullyChargedBowCrit) {
            arrow.setCritArrow(false);
        }
        Float critChance, critDamage;
        HashMap<String, Float> critData = getCritData(livingAttacker, false);
        critChance = critData.get("crit_chance");

        float critRoll = (float) Math.random();
        if(critChance > critRoll) {
            arrow.setCritArrow(true);
        }
        critDamage = critData.get("crit_damage");

        float postCritDamage = preCritDamage;

        if(arrow.isCritArrow()) {
            postCritDamage = preCritDamage * critDamage;
        }
        return postCritDamage;
    }
    public static float calculateMeleeCrit(LivingEntity livingAttacker, float preCritDamage) {

        HashMap<String, Float> critData = getCritData(livingAttacker, false);
        Float critDamage = critData.get("crit_damage");

        float postCritDamage;
        if(!livingAttacker.onGround() && livingAttacker.fallDistance > 0 && !Config.disableVanillaFallingCrit) {
            postCritDamage = preCritDamage * critDamage;
        }
        else {
            postCritDamage = simpleCritRoll(livingAttacker, false, preCritDamage);
        }
        return postCritDamage;
    }
    public static float simpleCritRoll(LivingEntity livingAttackerOrCaster, boolean isSpell, float preCritDamage) {
        float postCritDamage = preCritDamage;

        Float critChance, critDamage;
        HashMap<String, Float> critData = getCritData(livingAttackerOrCaster, isSpell);
        critChance = critData.get("crit_chance");
        critDamage = critData.get("crit_damage");

        float critRoll = (float) Math.random();
        if(critChance >= critRoll) {
            postCritDamage = preCritDamage * critDamage;
        }
        return postCritDamage;
    }
    public static float sumOfDamages(HashMap<String, Float> elementalData) {
        float sum = 0f;
        for(Map.Entry<String, Float> entry : elementalData.entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
    public static HashMap<String, Float> getAllElementalData(LivingEntity livingAttacker, LivingEntity livingDefender, boolean isSpell, Map.Entry<String, Float> otherDamage) {

        HashMap<String, Float> result = new HashMap<>();

        HashMap<String, Float> baseDamage = getBaseElementalDamagesData(livingAttacker, isSpell, otherDamage);
        HashMap<String, Float> elementalIncreasesAndDecreases = getElementalIncreasesAndDecreasesData(livingAttacker, isSpell);
        HashMap<String, Float> elementalMoreAndLessModifiers = getElementalMoreAndLessModifiersData(livingAttacker, isSpell);
        HashMap<String, Float> enemyElementalResistances = getElementalResistances(livingDefender);

        result.putAll(baseDamage);
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
                Float postResistanceDamageMultiplier = elementalMoreAndLessModifiers.get(key);
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
    public static HashMap<String, Float> getElementalIncreasesAndDecreasesData(LivingEntity livingAttacker, boolean isSpell) {
        HashMap<String, Float> elementalIncreasesAndDecreasesData = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.MULTIPLY_BASE);
        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute spellOrAttackAttribute = ModAttributes.getAttribute(String.format("%s:%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, spellOrAttack));

        if(spellOrAttackAttribute != null && livingAttacker.getAttribute(spellOrAttackAttribute) != null) {
            List<AttributeModifier> increaseOrDecreaseSpellOrAttackModifiers = livingAttacker.getAttribute(spellOrAttackAttribute).getModifiers()
                    .stream()
                    .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                    .toList();

            float sum = 0;
            for(AttributeModifier increaseOrDecreaseSpellOrAttackModifier : increaseOrDecreaseSpellOrAttackModifiers) {
                sum += (float) increaseOrDecreaseSpellOrAttackModifier.getAmount();
            }

            for(Map.Entry<String, Float> entry : elementalIncreasesAndDecreasesData.entrySet()) {
                elementalIncreasesAndDecreasesData.put(entry.getKey(), entry.getValue() + sum);
            }
        }

        return elementalIncreasesAndDecreasesData;
    }
    public static HashMap<String, Float> getElementalMoreAndLessModifiersData(LivingEntity livingAttacker, boolean isSpell) {
        HashMap<String, Float> elementalMoreAndLessModifiers = getElementalDataForGivenOperation(livingAttacker, isSpell, AttributeModifier.Operation.MULTIPLY_TOTAL);
        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute spellOrAttackAttribute = ModAttributes.getAttribute(String.format("%s:%s_damage_multiplier", ElementalAttackDamageCompatMod.MOD_ID, spellOrAttack));

        if(spellOrAttackAttribute != null && livingAttacker.getAttribute(spellOrAttackAttribute) != null) {
            List<AttributeModifier> moreOrLessSpellOrAttackModifiers = livingAttacker.getAttribute(spellOrAttackAttribute).getModifiers()
                    .stream()
                    .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .toList();

            float product = 1;
            for(AttributeModifier moreOrLessModifier : moreOrLessSpellOrAttackModifiers) {
                product *= (float) (1 + moreOrLessModifier.getAmount());
            }

            for(Map.Entry<String, Float> entry : elementalMoreAndLessModifiers .entrySet()) {
                elementalMoreAndLessModifiers .put(entry.getKey(), entry.getValue() * product);
            }
        }

        return elementalMoreAndLessModifiers;
    }
    private static HashMap<String, Float> getElementalResistances(LivingEntity livingDefender) {
        HashMap<String, Float> elementalResistanceData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            //Enemy resistances
            Float elementalResistance = ModAttributes.getAttributeValue(livingDefender, String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName));
            if(elementalResistance == null) elementalResistance = 1f; //by default, what elemental resistances are.

            elementalResistanceData.put(elementalAttributeName, elementalResistance);
        }

        return elementalResistanceData;
    }

    private static List<AttributeModifier> filterElementalAttributeModifiersByOperation(LivingEntity livingAttacker, String elementalAttributeName, boolean isSpell, AttributeModifier.Operation operation) {
        String spellOrAttack = isSpell ? "spell" : "attack";
        Attribute elementalDamageAttribute = ModAttributes.getAttribute(String.format("%s:%s_%s_damage",
                ElementalAttackDamageCompatMod.MOD_ID,
                elementalAttributeName,
                spellOrAttack
        ));

        if(elementalDamageAttribute != null && livingAttacker.getAttribute(elementalDamageAttribute) != null) {

            return livingAttacker.getAttribute(elementalDamageAttribute).getModifiers()
                    .stream()
                    .filter(attribute -> attribute.getOperation() == operation)
                    .toList();
        }
        return null;
    }
    private static HashMap<String, Float> getElementalDataForGivenOperation(LivingEntity livingAttacker, boolean isSpell, AttributeModifier.Operation operation) {

        HashMap<String, Float> elementalData = new HashMap<>();

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            List<AttributeModifier> addedDamageAttributeModifiersForElement = filterElementalAttributeModifiersByOperation(livingAttacker, elementalAttributeName, isSpell, operation);

            if(addedDamageAttributeModifiersForElement != null) {

                if(operation == AttributeModifier.Operation.ADDITION || operation == AttributeModifier.Operation.MULTIPLY_BASE) {
                    float sum = operation == AttributeModifier.Operation.MULTIPLY_BASE ? 1 : 0;
                    for (var attributeModifier : addedDamageAttributeModifiersForElement) {
                        sum += (float) attributeModifier.getAmount();
                    }
                    elementalData.put(elementalAttributeName, sum);
                }
                else {
                    float product = 1;
                    for (var attributeModifier : addedDamageAttributeModifiersForElement) {
                        product *= (float) (1 + attributeModifier.getAmount());
                    }
                    elementalData.put(elementalAttributeName, product);
                }
            }
            else {
                elementalData.put(elementalAttributeName, 0f);
            }
        }
        return elementalData;
    }
    public static HashMap<String, Float> getCritData(LivingEntity livingAttackerOrCaster, boolean isSpell) {

        HashMap<String, Float> critData = new HashMap<>();
        Float critChance, critDamage;

        if(isSpell) {
            critChance = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.spellCritChanceAttributeId);
            if(critChance == null) critChance = EventHandler.cc;
            critDamage = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.spellCritDamageAttributeId);
            if(critDamage == null) critDamage = EventHandler.cd;

            if(Config.applyAttackCritAttributesGlobally) {
                Float secondaryCritChance = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.attackCritChanceAttributeId);
                if(secondaryCritChance == null) secondaryCritChance = 0f;
                Float secondaryCritDamage = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.attackCritDamageAttributeId);
                if(secondaryCritDamage == null) secondaryCritDamage = 0f;

                critChance += secondaryCritChance + (float) Config.modCompatCritChanceOffset;
                critDamage += secondaryCritDamage + (float) Config.modCompatCritDamageOffset;
            }
            else {
                Float secondaryCritChance = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.globalCritChanceAttributeId);
                if(secondaryCritChance == null) secondaryCritChance = 0f;
                Float secondaryCritDamage = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.globalCritDamageAttributeId);
                if(secondaryCritDamage == null) secondaryCritDamage = 0f;

                critChance += secondaryCritChance + (float) Config.modCompatCritChanceOffset;
                critDamage += secondaryCritDamage + (float) Config.modCompatCritDamageOffset;
            }
        }
        else {
            critChance = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.attackCritChanceAttributeId);
            if(critChance == null) critChance = EventHandler.cc;
            critDamage = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.attackCritDamageAttributeId);
            if(critDamage == null) critDamage = EventHandler.cd;

            if(!Config.applyAttackCritAttributesGlobally) {
                Float secondaryCritChance = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.globalCritChanceAttributeId);
                if(secondaryCritChance == null) secondaryCritChance = 0f;
                Float secondaryCritDamage = ModAttributes.getAttributeValue(livingAttackerOrCaster, Config.globalCritDamageAttributeId);
                if(secondaryCritDamage == null) secondaryCritDamage = 0f;

                critChance += secondaryCritChance + (float) Config.modCompatCritChanceOffset;
                critDamage += secondaryCritDamage + (float) Config.modCompatCritDamageOffset;
            }
        }

        critData.put("crit_chance", critChance);
        critData.put("crit_damage", critDamage);

        return critData;
    }
}
