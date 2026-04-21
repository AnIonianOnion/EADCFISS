package com.anionianonion.elementalattackdamagecompat;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentApplier;
import com.anionianonion.elementalattackdamagecompat.ailments.DoTDamageSource;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.fml.ModList;

import java.util.*;

import static java.lang.Math.max;

public class DamageManager {

    //returns false if LivingHurtEvent is triggered by an "attack", aka. with a melee/ranged weapon.
    public static boolean hasFailedInitialChecks(DamageSource damageSource) {
        if(ElementalAttackDamageCompatMod.IS_RANDOM_DAMAGE_MOD_ENABLED) return true; //don't calculate damage if the user has this mod, as the mod does this step instead.

        //if(damageSource.isIndirect()) return; //only affects arrows and not sweeping
        if (damageSource.toString().contains("(sweeping)")) return true;

        if(damageSource instanceof DoTDamageSource) return true;

        var attacker = damageSource.getEntity();
        if(!(attacker instanceof LivingEntity)) return true;

        return damageSource instanceof SpellDamageSource; //simplified from an if statement, because it is the last check, and we can directly return the value as a result.
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
                .filter(entity -> entity.distanceToSqr(player) < max(sweepingEdgeHitbox.maxX, sweepingEdgeHitbox.maxZ))
                .filter(entity -> !entity.isInvulnerable())
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

                if(enchantmentName.equals("minecraft:sweeping")) {
                    sweepingLevel = enchantment.getInt("Lvl");
                    break;
                }
            }
        }
        return sweepingLevel;
    }
    public static void performSweepingAttack(int sweepingLevel, float penultimateDamage, DamageSource damageSource, List<LivingEntity> nearbyEnemies, LivingDamageEvent e, HashMap<String, Float> elementalDamageData, boolean isCrit) {
        float roundedPenultimateDamage = Math.round(penultimateDamage);
        float finalDamage = Config.roundFinalDamage ? roundedPenultimateDamage : penultimateDamage;
        e.setAmount(finalDamage);

        float sweepingRatio = ((float) sweepingLevel / (sweepingLevel + 1));
        float sweepingDamage = 1 + penultimateDamage * sweepingRatio;
        float roundedSweepingDamage = Math.round(sweepingDamage);
        float finalSweepingDamage = Config.roundFinalDamage ? roundedSweepingDamage : sweepingDamage;

        // Create a sweeping-specific damage source
        DamageSource sweepingSource = new DamageSource(damageSource.typeHolder(), damageSource.getDirectEntity()) {
            @Override
            public String toString() {
                return damageSource + " (sweeping)";
            }
        };

        var sweepingData = copy(elementalDamageData);
        AttributeHelpers.multiplyWithMultiplier(sweepingData, sweepingRatio);

        nearbyEnemies.forEach(nearbyEnemy -> {
          nearbyEnemy.hurt(sweepingSource, finalSweepingDamage);

            if(isCrit) {
                AilmentApplier.critApplyAllAilmentsFromDamage(
                        (LivingEntity) damageSource.getEntity(),
                        nearbyEnemy,
                        sweepingData
                );
            }
            else {
                AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(
                        (LivingEntity) damageSource.getEntity(),
                        nearbyEnemy,
                        sweepingData);
            }
        });
    }

    public static void manageArrowShot(LivingDamageEvent e) {
        DamageSource damageSource = e.getSource();
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();

        Arrow arrow = (Arrow) damageSource.getDirectEntity();
        assert arrow != null;

        LivingEntity livingDefender = e.getEntity();
        float minecraftArrowBaseDamage = (float) arrow.getBaseDamage();
        var data = AttributeHelpers.getBasicElementalData(livingAttacker, livingDefender,false, Map.entry("physical", minecraftArrowBaseDamage));

        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, false);
        AttributeHelpers.multiplyWithArrowSpeed(data, arrow);
        AttributeHelpers.multiplyWithCritDamageForArrowsIfCrit(data, livingAttacker, livingDefender, arrow);
        AttributeHelpers.applyLessDamageFromPossibleSapEffects(data, livingAttacker);

        float newBaseFlatDamage = sumOfDamages(data);

        int roundedDamage = Math.round(newBaseFlatDamage);
        float finalDamage = Config.roundFinalDamage ? roundedDamage : newBaseFlatDamage;

        e.setAmount(finalDamage);
        if(arrow.isCritArrow()) AilmentApplier.critApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
        else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
    }
    public static void manageOtherSpells(LivingDamageEvent e) {
        DamageSource damageSource = e.getSource();
        LivingEntity livingCaster = (LivingEntity) damageSource.getEntity();
        var livingDefender = e.getEntity();

        float baseSpellDamage = e.getAmount();
        var data = AttributeHelpers.getBasicElementalData(livingCaster, livingDefender, true, Map.entry("physical", baseSpellDamage));

        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, true);
        AttributeHelpers.multiplyWithCritDamageIfCrit(data, livingCaster, livingDefender, true);

        var preCritData = copy(data);
        boolean isCrit = isCrit(preCritData, data);
        AttributeHelpers.applyLessDamageFromPossibleSapEffects(data, livingCaster);

        float newBaseSpellDamage = sumOfDamages(data);

        int roundedDamage = Math.round(newBaseSpellDamage);
        float finalDamage = Config.roundFinalDamage ? roundedDamage : newBaseSpellDamage;
        e.setAmount(finalDamage);
        if(isCrit) AilmentApplier.critApplyAllAilmentsFromDamage(livingCaster, livingDefender, data);
        else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(livingCaster, livingDefender, data);

    }
    public static void managePlayerMelee(LivingDamageEvent e) {

        DamageSource damageSource = e.getSource();
        Player player = (Player) damageSource.getEntity();
        assert player != null;

        LivingEntity livingDefender = e.getEntity();
        float baseDamage = e.getAmount();

        //"physical" damage
        float expectedBaseDamage = AttributeHelpers.getBaseTotal(player, Attributes.ATTACK_DAMAGE);
        if(baseDamage > expectedBaseDamage) baseDamage = expectedBaseDamage; //stops crit damage from other mods from being applied twice
        var data = AttributeHelpers.getBasicElementalData(player, livingDefender, false, Map.entry("physical", baseDamage));

        if(Config.enableDebugMode) {

            player.sendSystemMessage(Component.literal("DamageSource to String: " + damageSource));
            player.sendSystemMessage(Component.literal("DamageType: " + damageSource.type()));
            player.sendSystemMessage(Component.literal("DamageSource msgId: " + damageSource.getMsgId()));
            ElementalAttackDamageCompatMod.LOGGER.info(damageSource.toString());
            ElementalAttackDamageCompatMod.LOGGER.info("Melee player damage event fired");
        }

        var preCritData = copy(data);
        AttributeHelpers.multiplyWithCritDamageIfMeleeCrit(data, player, livingDefender);
        boolean isCrit = isCrit(preCritData, data);
        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, false);
        AttributeHelpers.applyLessDamageFromPossibleSapEffects(data, player);

        float penultimateDamage = sumOfDamages(data);

        int roundedDamage = Math.round(penultimateDamage); //remember that base damage already checks for crit.
        float finalDamage = Config.roundFinalDamage ? roundedDamage : penultimateDamage;
        ItemStack itemStack = player.getItemInHand(player.getUsedItemHand());
                            //!player.onGround() removed, and brought outside as a coefficient, because it was inverted twice
        boolean isFallCrit = //!player.onGround() &&
            player.fallDistance > 0 && !player.isInWater() && !player.onClimbable() && !player.hasEffect(MobEffects.BLINDNESS) && !Config.disableVanillaFallingCrit;
        boolean playerCanSweep = itemStack.canPerformAction(ToolActions.SWORD_SWEEP) //right tool
                && player.onGround()
                && !isFallCrit //not fallCritting
                && player.getAttackStrengthScale(0.5f) > 0.9f //near full charge
                && !player.isSprinting(); //not sprinting

        boolean isProjectWarDanceInstalled = ModList.get().isLoaded("wardance");
        boolean isEpicFightInstalled = ModList.get().isLoaded("epicfight");
        boolean sweepingHandledByOtherMods = isEpicFightInstalled || isProjectWarDanceInstalled;



        if(playerCanSweep && !sweepingHandledByOtherMods) {
            List<LivingEntity> nearbyEnemies = getNearbyEnemies(player, livingDefender);
            int sweepingLevel = getSweepingLevelOfPlayerWeapon(player);

            performSweepingAttack(sweepingLevel, finalDamage, damageSource, nearbyEnemies, e, data, isCrit); //baseDamage

            if(isCrit) AilmentApplier.critApplyAllAilmentsFromDamage(player, livingDefender, data);
            else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(player, livingDefender, data);
        }
        else {
            e.setAmount(finalDamage);
            if(isCrit) AilmentApplier.critApplyAllAilmentsFromDamage(player, livingDefender, data);
            else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(player, livingDefender, data);
        }
    }
    public static void manageNonPlayerMelee(LivingDamageEvent e) {

        DamageSource damageSource = e.getSource();
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        LivingEntity livingDefender = e.getEntity();
        float baseDamage = e.getAmount();

        var data = AttributeHelpers.getBasicElementalData(livingAttacker, livingDefender, false, Map.entry("physical", baseDamage));

        if(Config.enableDebugMode) {
            ElementalAttackDamageCompatMod.LOGGER.info(damageSource.toString());
            ElementalAttackDamageCompatMod.LOGGER.info("Other melee damage event fired");
        }

        assert livingAttacker != null;

        var preCritData = copy(data);
        AttributeHelpers.multiplyWithCritDamageIfMeleeCrit(data, livingAttacker, livingDefender);
        boolean isCrit = isCrit(preCritData, data);
        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, false);
        AttributeHelpers.applyLessDamageFromPossibleSapEffects(data, livingAttacker);

        float newBaseFlatDamage = sumOfDamages(data);

        int roundedDamage = Math.round(newBaseFlatDamage); //remember that base damage already checks for crit.
        float finalDamage = Config.roundFinalDamage ? roundedDamage : newBaseFlatDamage;

        e.setAmount(finalDamage);
        if(isCrit) AilmentApplier.critApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
        else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
    }
    private static void manageOtherProjectiles(LivingDamageEvent e) {
        //RANGED: OTHER PROJECTILES
        DamageSource damageSource = e.getSource();
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        LivingEntity livingDefender = e.getEntity();

        if(Config.enableDebugMode) {
            ElementalAttackDamageCompatMod.LOGGER.info(damageSource.toString());
            ElementalAttackDamageCompatMod.LOGGER.info("Other projectiles damage event fired");
        }
        float baseDamage = e.getAmount();

        //TaCZ guns trigger this twice,
        // with gun damage split into two instances, with my added modifiers counted per instance
        // so we need to get the split base damage twice, and add our added modifiers divided by 2 twice.

        HashMap<String, Float> data;
        float newBaseFlatDamage;
        if(isTaCZ_Bullet(damageSource)) {
            data = AttributeHelpers.getBasicElementalData(livingAttacker, livingDefender, false, Map.entry("physical", 0f));
            //crit for these bullets are rolled independently for each part, and does not give the expected crit damage when it does crit.
        }
        else {
            data = AttributeHelpers.getBasicElementalData(livingAttacker, livingDefender, false, Map.entry("physical", baseDamage));
        }

        var preCritData = copy(data);
        AttributeHelpers.multiplyWithCritDamageIfCrit(data, livingAttacker, livingDefender, false);
        boolean isCrit = isCrit(preCritData, data);
        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, false);
        AttributeHelpers.applyLessDamageFromPossibleSapEffects(data, livingAttacker);

        if(isTaCZ_Bullet(damageSource)) {
            //crit for these bullets are rolled independently for each part, and does not give the expected crit damage when it does crit.
            newBaseFlatDamage = baseDamage + sumOfDamages(data) / 2;
        }
        else {
            newBaseFlatDamage = sumOfDamages(data);
        }

        int roundedDamage = Math.round(newBaseFlatDamage);
        float finalDamage = Config.roundFinalDamage ? roundedDamage : newBaseFlatDamage;
        e.setAmount(finalDamage);
        if(isCrit) AilmentApplier.critApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
        else AilmentApplier.nonCritTryToApplyAllAilmentsFromDamage(livingAttacker, livingDefender, data);
    }
    public static void manageMeleeAndOtherProjectiles(LivingDamageEvent e) {

        DamageSource damageSource = e.getSource();

        //we can cast because we are checking already in hasFailedInitialChecks() that e.getSource().getEntity() is an instance of LivingEntity.
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        Entity directEntity = damageSource.getDirectEntity();

        //ElementApplier.applyAllFromAttacker(livingDefender, 0, 80, data);
        //----MELEE---- ONLY FOR PLAYERS
        if(livingAttacker instanceof Player player && directEntity == player) {
            managePlayerMelee(e);
        }
        //MELEE (NON-PLAYERS)
        else if(directEntity == livingAttacker && livingAttacker != null) {
            manageNonPlayerMelee(e);
        }
        else {
            manageOtherProjectiles(e);
        }
    }

    //--------------------------------------------------------DAMAGE CALCULATIONS--------------------------------------------------------

    public static float sumOfDamages(HashMap<String, Float> elementalData) {
        float sum = 0f;
        for(Map.Entry<String, Float> entry : elementalData.entrySet()) {
            sum += entry.getValue();
        }
        return sum;
    }

    private static boolean isTaCZ_Bullet(DamageSource damageSource) {
        boolean isTaCZ_Bullet = false;
        for(var key : DamageSourcesCompat.taczBullets) {
            if(damageSource.is(key)) {
                isTaCZ_Bullet = true;
                break;
            }
        }

        return isTaCZ_Bullet;
    }

    public static boolean isCustomSpellDamageSource(DamageSource damageSource) {
        boolean isOtherSpell = false;
        for(var otherSpellRegistryKey : DamageSourcesCompat.otherSpellDamageSources) {
            if(damageSource.is(otherSpellRegistryKey)) {
                isOtherSpell = true;
                break;
            }
        }
        return isOtherSpell;
    }

    public static HashMap<String, Float> copy(HashMap<String, Float> elementalData) {

        return new HashMap<>(elementalData);
    }

    public static boolean isCrit(HashMap<String, Float> preCritData, HashMap<String, Float> postCritData) {
        var a = sumOfDamages(preCritData);
        var b = sumOfDamages(postCritData);

        return b > a;
    }
}
