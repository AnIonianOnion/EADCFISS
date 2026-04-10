package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.Element;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AilmentApplier {

    private static void applySingleAilment(LivingEntity livingAttackerOrCaster, LivingEntity target, Ailment ailment, float damage) {
        AilmentEffect effect = AilmentEffectRegistry.get(ailment);

        if (effect instanceof FreezeEffect freezeEffect && target instanceof Mob mob) {
            int duration = (int) (freezeEffect.computeVariableEffectDuration(damage, target, livingAttackerOrCaster) * 20);

            AilmentDataHelper.getOptional(target).ifPresent(cap -> {
                cap.addAilment(ailment, effect, damage, duration);
                AilmentInstance instance = cap.getAilments().get(ailment);
                instance.storedTarget = mob.getTarget();
            });
        }
        else if (effect instanceof NonDamagingAilmentEffect ndae) {
            float effectStrength = ndae.computeVariableEffectStrength(damage, target, livingAttackerOrCaster);
            int duration = getDurationInTicks(ndae, livingAttackerOrCaster);

            AilmentDataHelper.getOptional(target).ifPresent(cap -> {
                cap.addAilment(ailment, effect, damage, duration);
                AilmentInstance instance = cap.getAilments().get(ailment);
                instance.effectStrength = effectStrength;
            });

        }
        else {
            int duration = (int) (effect.getDurationInSeconds(livingAttackerOrCaster) * 20);

            AilmentDataHelper.getOptional(target).ifPresent(cap -> {
                cap.addAilment(ailment, effect, damage, duration);
            });
        }
    }

    public static void critApplyAllAilmentsFromDamage(
            LivingEntity attacker,
            LivingEntity target,
            HashMap<String, Float> elementalData
    ) {
        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        for (Map.Entry<String, Float> entry : elementalData.entrySet()) {

            String key = entry.getKey().toUpperCase();
            float damage = entry.getValue();

            // Skip elements with no damage
            if (damage <= 0) continue;

            // Convert string → Element enum
            Element element;
            try {
                element = Element.valueOf(key);
            } catch (IllegalArgumentException e) {
                continue; // unknown element name
            }

            // Determine ailments for this element
            List<Ailment> ailments = AilmentResolver.determineAilments(attacker, element);

            // Apply each ailment
            for (Ailment ailment : ailments) {

                int stacks = 1 + mods.extraStacks(ailment);

                for (int i = 0; i < stacks; i++) {
                    applySingleAilment(attacker, target, ailment, damage);
                }
            }
        }
    }

    public static void nonCritTryToApplyAllAilmentsFromDamage(
            LivingEntity attacker,
            LivingEntity target,
            HashMap<String, Float> elementalData
    ) {
        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        for (Map.Entry<String, Float> entry : elementalData.entrySet()) {

            String key = entry.getKey().toUpperCase();
            float damage = entry.getValue();

            // Skip elements with no damage
            if (damage <= 0) continue;

            // Convert string → Element enum
            Element element;
            try {
                element = Element.valueOf(key);
            } catch (IllegalArgumentException e) {
                continue; // unknown element name
            }

            // Determine ailments for this element
            List<Ailment> ailments = AilmentResolver.determineAilments(attacker, element);

            // Apply each ailment
            for (Ailment ailment : ailments) {

                if(Config.enableDebugMode)
                    ElementalAttackDamageCompatMod.LOGGER.info(ailment.name());

                int stacks = 1 + mods.extraStacks(ailment);

                for (int i = 0; i < stacks; i++) {
                    Float chanceToSucceed = ModAttributes.getAttributeValue(attacker, String.format("%s:chance_to_%s", ElementalAttackDamageCompatMod.MOD_ID, ailment.name().toLowerCase()));
                    if(chanceToSucceed == null) chanceToSucceed = 0f;

                    float roll = (float) Math.random();
                    if(chanceToSucceed >= roll) applySingleAilment(attacker, target, ailment, damage);
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

    private static void addNonFreezeNonDamagingAilmentEffect(float effectStrength, int durationInTicks) {

    }

}


