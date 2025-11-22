package com.anionianonion.elementalattackdamagecompat;

import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
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

    public static boolean simpleCritRoll(LivingEntity livingAttackerOrCaster, boolean isSpell) {

        Float critChance;
        HashMap<String, Float> critData = getCritData(livingAttackerOrCaster, isSpell);
        critChance = critData.get("crit_chance");

        float critRoll = (float) Math.random();
        if(critChance >= critRoll) {
            return true;
        }
        return false;
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
    public static void performSweepingAttack(int sweepingLevel, float critAdjustedDamage, LivingEntity attackedEntity, DamageSource damageSource, List<LivingEntity> nearbyEnemies, LivingHurtEvent e) {
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

    public static void manageArrowShot(LivingEntity livingAttacker, Arrow arrow, float baseTotalElementalDamage, LivingHurtEvent e) {
        float minecraftArrowBaseDamage = (float) arrow.getBaseDamage();
        float baseFlatDamage = minecraftArrowBaseDamage + baseTotalElementalDamage;
        float flightSpeededAdjustedDamage = (float) (baseFlatDamage * arrow.getDeltaMovement().length()); //speed
        float postCritAdjustedDamage = DamageManager.calculateCritArrow(arrow, livingAttacker, flightSpeededAdjustedDamage);

        int roundedDamage = Math.round(postCritAdjustedDamage);
        e.setAmount(roundedDamage);
    }
    public static void manageMeleeAndOtherProjectiles(LivingEntity livingAttacker, Entity directEntity, DamageSource damageSource, float baseTotalElementalDamage, LivingHurtEvent e) {
        float baseDamage = e.getAmount();

        float expectedBaseDamage = (float) livingAttacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean vanillaCritApplied = baseDamage > expectedBaseDamage;

        float newBaseFlatDamage;
        if(vanillaCritApplied) {
            newBaseFlatDamage = (baseDamage / 1.5f) + baseTotalElementalDamage;
        }
        else newBaseFlatDamage = baseDamage + baseTotalElementalDamage;

        ElementalAttackDamageCompatMod.LOGGER.info("" + baseDamage);
        LivingEntity attackedEntity = e.getEntity();
        //----MELEE---- ONLY FOR PLAYERS
        if(livingAttacker instanceof Player player && directEntity == player) {

            float critAdjustedDamage = DamageManager.calculateMeleeCrit(player, newBaseFlatDamage);
            ElementalAttackDamageCompatMod.LOGGER.info("crit adj: " + critAdjustedDamage);
            ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());

            if(itemStack.canPerformAction(ToolActions.SWORD_SWEEP)) {
                List<LivingEntity> nearbyEnemies = DamageManager.getNearbyEnemies(player, attackedEntity);
                int sweepingLevel = DamageManager.getSweepingLevelOfPlayerWeapon(player);
                performSweepingAttack(sweepingLevel, critAdjustedDamage, attackedEntity, damageSource, nearbyEnemies, e);
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

    public static float calculateBaseTotalElementalDamageFromAttacksPostElementalResistances(LivingEntity livingAttacker, LivingEntity livingDefender) {

        var totalElementalDamage = 0f;
        //accumulator for post-resistance damage
        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            Float elementalDamage = ModAttributes.getAttributeValue(
                    livingAttacker,
                    String.format("%s:%s_attack_damage",
                            ElementalAttackDamageCompatMod.MOD_ID,
                            elementalAttributeName)
            );
            //expected: elementalDamage is -1 if damageDealer is not a player.
            if(elementalDamage != null) {
                Float enemyElementalResistance = ModAttributes.getAttributeValue(
                        livingDefender,
                        String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName)
                );
                if(enemyElementalResistance == null) {
                    enemyElementalResistance = 1f; //by default, what elemental resistances are.
                }
                elementalDamage *= (2 - enemyElementalResistance);
                totalElementalDamage += elementalDamage;
            }
        }

        return totalElementalDamage;
    }
    public static float calculateBaseTotalElementalDamageFromSpellsPostElementalResistances(SpellDamageSource spellDamageSource, LivingEntity livingDefender, float originalTotalDamage) {

        var spellSchool = spellDamageSource.spell().getSchoolType().getId().getPath();
        var caster = (LivingEntity) spellDamageSource.get().getEntity();
        var totalElementalDamage = 0f;

        for(String elementalAttributeName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            Float elementalDamage = ModAttributes.getAttributeValue(
                    caster,
                    String.format("%s:%s_spell_damage",
                            ElementalAttackDamageCompatMod.MOD_ID,
                            elementalAttributeName)
            );
            if(elementalDamage != null) {

                //the original spell does not have an elemental damage from our mod by default, so we get it based on school
                if(spellSchool.equals(elementalAttributeName)) {
                    //if the spell's school is found, we classify it as the corresponding elemental damage from our mod.
                    // So with the original damage, we remove it, and add it to ours instead.

                    //don't know if spell power is calculated into spell's original damage by default.
                    //if it isn't, then originalDamage = originalDamgage * spellPower. //results showed it does not scale linearly

                    //bc elementalDamage isn't null, we can assume our attribute isn't null.
                    Attribute elementalDamageAttribute = ModAttributes.getAttribute(String.format("%s:%s_spell_damage",
                            ElementalAttackDamageCompatMod.MOD_ID,
                            elementalAttributeName));

                    //even if elementalDamage != null, check if caster has the attribute
                    if(caster.getAttribute(elementalDamageAttribute) != null)
                    {
                        var all = caster.getAttribute(elementalDamageAttribute).getModifiers();
                        var increasesOrDecreases = all
                                .stream()
                                .filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE)
                                .toList();
                        var moreOrLessMods = all
                                .stream()
                                .filter(modifier -> modifier.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                                .toList();

                        float totalIncrease = 0;
                        for(var modifier : increasesOrDecreases) {
                            totalIncrease += (float) modifier.getAmount();
                        }

                        float productMore = 1;
                        for(var modifier : moreOrLessMods) {
                            productMore *= (float) modifier.getAmount();
                        }

                        originalTotalDamage = originalTotalDamage * (1 + totalIncrease) * productMore;
                    }

                    elementalDamage += originalTotalDamage;
                    originalTotalDamage -= originalTotalDamage; //aka 0
                }

                //Enemy resistances
                Float enemyElementalResistance = ModAttributes.getAttributeValue(
                        livingDefender,
                        String.format("irons_spellbooks:%s_magic_resist", elementalAttributeName)
                );
                if(enemyElementalResistance == null) {
                    enemyElementalResistance = 1f; //by default, what elemental resistances are.
                }

                //todo: don't know if spell power is calculated into spell's original damage by default.
                //if it isn't, then originalDamage = originalDamgage * spellPower.

                elementalDamage *= (2 - enemyElementalResistance);
                totalElementalDamage += elementalDamage;
            }
            else {
                ElementalAttackDamageCompatMod.LOGGER.info("Unable to get attribute from entity because elementalDamage is null");
            }
        }

        if(totalElementalDamage != 0)
            return totalElementalDamage;
        else {
            return originalTotalDamage;
        }
    }

    /*
    public static float calculateIncreasesAndMoreMultipliers(LivingEntity livingAttacker, boolean isSpell) {

        if(isSpell) {

        }
        else {

        }
    }*/

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
