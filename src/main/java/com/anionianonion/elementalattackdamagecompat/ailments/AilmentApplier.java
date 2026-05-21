package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentApplier {

    private static void applySingleAilment(LivingEntity livingAttackerOrCaster, LivingEntity livingDefender, String ailmentKey, float damage) {

        String finalAilmentKey = normalize(ailmentKey);
        AilmentEffect effect = AilmentEffectRegistry.getEffect(finalAilmentKey);

        if(effect instanceof NonDamagingAilmentEffect ndae) {

            float effectStrength = ndae.isUsingVaryingEffectStrength() ?
                    ndae.computeVariableEffectStrength(damage, livingDefender, livingAttackerOrCaster) :
                    ndae.getBasicEffectStrength();

            int effectDurationInTicks = ndae.isUsingVaryingEffectDuration() ?
                    (int) (ndae.computeVariableEffectDuration(damage, livingDefender, livingAttackerOrCaster) * 20) :
                    getDurationInTicks(ndae, livingAttackerOrCaster);

            AilmentDataHelper.getOptional(livingDefender).ifPresent(cap -> {
                AilmentInstance instance = new AilmentInstance(ndae, damage, effectStrength, effectDurationInTicks);
                if(livingDefender instanceof Mob mob) {
                    instance.storedTarget = mob.getTarget();
                }
                if(Config.enableDebugMode) {
                    ElementalAttackDamageCompatMod.LOGGER.info("Ailment: " + ailmentKey);
                    ElementalAttackDamageCompatMod.LOGGER.info("effect strength: " + effectStrength);
                }

                cap.addAilment(finalAilmentKey, instance, livingDefender);
            });
        }
        else {
            int duration = getDurationInTicks(effect, livingAttackerOrCaster);

            AilmentDataHelper.getOptional(livingDefender).ifPresent(cap -> {
                AilmentInstance instance = new AilmentInstance(effect, damage, 0, duration);
                cap.addAilment(finalAilmentKey, instance, livingDefender);
            });
        }
    }

    public static void critApplyAllAilmentsFromDamage(LivingEntity attacker, LivingEntity target, HashMap<String, Float> elementalData) {
        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        for (Map.Entry<String, Float> entry : elementalData.entrySet()) {

            String element = entry.getKey();
            float damage = entry.getValue();

            // Skip elements with no damage
            if (damage <= 0) continue;

            // Determine ailments for this element
            List<String> ailments = AilmentResolver.determineAilments(attacker, element);

            // Apply each ailment
            for (String ailment : ailments) {

                int stacks = 1 + mods.getExtraStacks(ailment);

                for (int i = 0; i < stacks; i++) {
                    applySingleAilment(attacker, target, ailment, damage);
                }
            }
        }
    }

    public static void nonCritTryToApplyAllAilmentsFromDamage(LivingEntity attacker, LivingEntity target, HashMap<String, Float> elementalData) {
        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        for (Map.Entry<String, Float> entry : elementalData.entrySet()) {

            String element = entry.getKey();
            float damage = entry.getValue();

            // Skip elements with no damage
            if (damage <= 0) continue;

            // Determine ailments for this element
            List<String> ailments = AilmentResolver.determineAilments(attacker, element);

            // Apply each ailment
            for (String ailment : ailments) {

                if(Config.enableDebugMode) {
                    ElementalAttackDamageCompatMod.LOGGER.info(ailment);
                }

                int stacks = 1 + mods.getExtraStacks(ailment);

                for (int i = 0; i < stacks; i++) {
                    Float chanceToSucceed = ModAttributes.getAttributeValue(attacker, String.format("%s:chance_to_inflict_%s", ElementalAttackDamageCompatMod.MOD_ID, ailment));
                    if(chanceToSucceed == null) chanceToSucceed = 0f;

                    float roll = (float) Math.random();
                    if(chanceToSucceed >= roll) applySingleAilment(attacker, target, ailment, damage);
                }

                if (Config.enableDebugMode) {
                    ElementalAttackDamageCompatMod.LOGGER.info(
                            "Element={}, ailment={}, stacks={}, alt={}, repl={}",
                            element,
                            ailment,
                            mods.getExtraStacks(ailment),
                            mods.getAlternateAilments(element),
                            mods.getReplacement(ailment)
                    );
                }

            }
        }
    }

    /**
        Freeze duration doesn't have a constant value,
        so this method can only be used on AilmentEffects with constant values.
     */
    private static int getDurationInTicks(AilmentEffect ae, LivingEntity livingAttackerOrCaster) {

        return (int) (ae.getDurationInSeconds(livingAttackerOrCaster) * 20);
    }
}


