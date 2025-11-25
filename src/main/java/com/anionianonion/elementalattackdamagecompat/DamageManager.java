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
        float newBaseFlatDamage = sumOfDamages(AttributeHelpers.getAllElementalData(livingAttacker, livingDefender, false, Map.entry("physical", minecraftArrowBaseDamage)));
        float flightSpeededAdjustedDamage = (float) (newBaseFlatDamage * arrow.getDeltaMovement().length()); //speed
        float postCritAdjustedDamage = calculateCritArrow(arrow, livingAttacker, flightSpeededAdjustedDamage);

        int roundedDamage = Math.round(postCritAdjustedDamage);
        e.setAmount(roundedDamage);
    }
    public static void manageMeleeAndOtherProjectiles(LivingEntity livingAttacker, Entity directEntity, DamageSource damageSource, LivingHurtEvent e) {
        float baseDamage = e.getAmount();

        //"physical" damage
        Attribute attackAttribute = Attributes.ATTACK_DAMAGE;
        List<AttributeModifier> addedAttackDamageModifiers = livingAttacker.getAttribute(attackAttribute).getModifiers()
                .stream()
                .filter(attributeModifier -> attributeModifier.getOperation() == AttributeModifier.Operation.ADDITION)
                .toList();
        float baseAddedPhys = 0;
        for(AttributeModifier attributeModifier : addedAttackDamageModifiers) {
            baseAddedPhys += (float) attributeModifier.getAmount();
        }

        float expectedBaseDamage = (float) livingAttacker.getAttribute(attackAttribute).getBaseValue() + baseAddedPhys;
        if(baseDamage > expectedBaseDamage) baseDamage = expectedBaseDamage; //stops crit damage from other mods from being applied twice

        LivingEntity livingDefender = e.getEntity();

        float newBaseFlatDamage = sumOfDamages(AttributeHelpers.getAllElementalData(livingAttacker, livingDefender, false, Map.entry("physical", baseDamage)));

        //----MELEE---- ONLY FOR PLAYERS
        if(livingAttacker instanceof Player player && directEntity == player) {

            float critAdjustedDamage = calculateMeleeCrit(player, newBaseFlatDamage);
            ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());

            if(itemStack.canPerformAction(ToolActions.SWORD_SWEEP)) {
                List<LivingEntity> nearbyEnemies = getNearbyEnemies(player, livingDefender);
                int sweepingLevel = getSweepingLevelOfPlayerWeapon(player);
                performSweepingAttack(sweepingLevel, critAdjustedDamage, damageSource, nearbyEnemies, e); //baseDamage
            }
            else {
                int roundedDamage = Math.round(critAdjustedDamage); //remember that base damage already checks for crit.
                e.setAmount(roundedDamage);
            }
        }
        //---RANGED---: OTHER PROJECTILES (PLAYERS AND NON-PLAYERS)
        //and ---MELEE---(NON-PLAYERS)
        else {
            float critAdjustedDamage;
            //MELEE (NON-PLAYERS)
            if(directEntity == livingAttacker) {
                critAdjustedDamage = calculateMeleeCrit(livingAttacker, newBaseFlatDamage);
            }
            //RANGED: OTHER PROJECTILES
            else {
                critAdjustedDamage = calculatePostCritDamage(livingAttacker, false, newBaseFlatDamage);
            }
            int roundedDamage = Math.round(critAdjustedDamage);
            e.setAmount(roundedDamage);
        }
    }

    public static float calculateCritArrow(Arrow arrow, LivingEntity livingAttacker, float preCritDamage) {
        //vanilla MC assumes that a fully charged bow or crossbow will automatically crit.
        if(arrow.isCritArrow() && Config.disableVanillaFullyChargedBowCrit) {
            arrow.setCritArrow(rollForIfAttacksOrSpellsCrit(livingAttacker, false));
        }
        Float critDamage;
        HashMap<String, Float> critData = AttributeHelpers.getCritData(livingAttacker, false);
        critDamage = critData.get("crit_damage");

        float postCritDamage = preCritDamage;
        if(arrow.isCritArrow()) {
            postCritDamage = preCritDamage * critDamage;
        }
        return postCritDamage;
    }
    public static float calculateMeleeCrit(LivingEntity livingAttacker, float preCritDamage) {

        HashMap<String, Float> critData = AttributeHelpers.getCritData(livingAttacker, false);
        Float critDamage = critData.get("crit_damage");

        float postCritDamage;
        if(!livingAttacker.onGround() && livingAttacker.fallDistance > 0 && !Config.disableVanillaFallingCrit) {
            postCritDamage = preCritDamage * critDamage;
        }
        else {
            postCritDamage = calculatePostCritDamage(livingAttacker, false, preCritDamage);
        }
        return postCritDamage;
    }
    public static float calculatePostCritDamage(LivingEntity livingAttackerOrCaster, boolean isSpell, float preCritDamage) {
        float postCritDamage = preCritDamage;

        Float critDamage;
        HashMap<String, Float> critData = AttributeHelpers.getCritData(livingAttackerOrCaster, isSpell);
        critDamage = critData.get("crit_damage");

        if(rollForIfAttacksOrSpellsCrit(livingAttackerOrCaster, isSpell)) {
            postCritDamage = preCritDamage * critDamage;
        }
        return postCritDamage;
    }

    public static boolean rollForIfAttacksOrSpellsCrit(LivingEntity livingAttacker, boolean isSpell) {
        Float critChance;
        HashMap<String, Float> critData = AttributeHelpers.getCritData(livingAttacker, isSpell);
        critChance = critData.get("crit_chance");

        float critRoll = (float) Math.random();
        return critChance >= critRoll;
    }

    public static float sumOfDamages(HashMap<String, Float> elementalData) {
        float sum = 0f;
        for(Map.Entry<String, Float> entry : elementalData.entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }
}
