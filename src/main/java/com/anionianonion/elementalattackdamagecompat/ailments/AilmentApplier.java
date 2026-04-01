package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Element;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.ShockEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AilmentApplier {

    private static void applySingleAilment(
            LivingEntity target,
            Ailment ailment,
            float damage
    ) {
        AilmentEffect effect = AilmentEffectRegistry.get(ailment);

        int duration;
        if(effect instanceof ShockEffect shockEffect) {
            duration = (int) shockEffect.getDurationSeconds(target.getLastAttacker());
        }
        else duration = DefaultAilmentDurations.get(ailment);

        AilmentDataHelper.getOptional(target).ifPresent(cap -> {
            cap.addAilment(ailment, effect, damage, duration);
        });
    }

    public static void applyAllAilmentsFromDamage(
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

                System.out.println(
                        "Applying ailment " + ailment +
                                " from element " + element +
                                " with damage " + damage
                );

                int stacks = 1 + mods.extraStacks(ailment);

                for (int i = 0; i < stacks; i++) {
                    applySingleAilment(target, ailment, damage);
                }
            }
        }
    }


}


