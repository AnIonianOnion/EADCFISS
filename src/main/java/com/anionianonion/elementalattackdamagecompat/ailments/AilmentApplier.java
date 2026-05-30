package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.util.ModUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentApplier {

    private static void applySingleAilment(LivingEntity livingAttackerOrCaster, LivingEntity livingDefender, String ailmentKey, float damage) {

        String finalAilmentKey = normalize(ailmentKey);
        AilmentEffect effect = AilmentEffectRegistry.getEffect(finalAilmentKey);

        int baseMaxStacks = effect.getDefaultMaxStacks();
        int extraMaxStacks = AilmentModifierHelper.get(livingAttackerOrCaster).getExtraMaxStacks(ailmentKey);
        int finalMaxStacks = baseMaxStacks + extraMaxStacks;

        if(livingAttackerOrCaster instanceof Player player && Config.enableDebugMode) {
            player.sendSystemMessage(Component.literal("extraMaxStacks: " + extraMaxStacks + ", finalMaxStacks: " + finalMaxStacks));
        }

        int effectDurationInTicks = ModUtils.getDurationInTicks(effect, damage, livingDefender, livingAttackerOrCaster);

        if(effect instanceof NonDamagingAilmentEffect ndae) {

            float effectStrength = ndae.isUsingVaryingEffectStrength() ?
                    ndae.computeVariableEffectStrength(damage, livingDefender, livingAttackerOrCaster) :
                    ndae.getBasicEffectStrength();

            AilmentDataHelper.getOptional(livingDefender).ifPresent(cap -> {
                AilmentInstance instance = new AilmentInstance(ndae, damage, effectStrength, effectDurationInTicks);
                /*
                if(livingDefender instanceof Mob mob) {
                    instance.storedTarget = mob.getTarget();
                }
                 */
                if(Config.enableDebugMode) {
                    ElementalAttackDamageCompatMod.LOGGER.info("Ailment: " + ailmentKey);
                    ElementalAttackDamageCompatMod.LOGGER.info("effect strength: " + effectStrength);
                }
                instance.setMaxStacks(finalMaxStacks);

                cap.addAilment(livingAttackerOrCaster, finalAilmentKey, instance, livingDefender);
            });
        }
        else {
            AilmentDataHelper.getOptional(livingDefender).ifPresent(cap -> {
                AilmentInstance instance = new AilmentInstance(effect, damage, 0, effectDurationInTicks);
                instance.setMaxStacks(finalMaxStacks);
                cap.addAilment(livingAttackerOrCaster, finalAilmentKey, instance, livingDefender);
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

                int stacksToApply = 1 + mods.getExtraStacks(ailment);

                for (int i = 0; i < stacksToApply; i++) {
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
}


