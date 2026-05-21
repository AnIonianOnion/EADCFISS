package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentResolver {

    /**
     * Determine which ailments should be applied for a given element,
     * taking into account attacker modifiers:
     *
     * - Alternate ailments override everything
     * - Replacements override defaults
     * - Defaults are used if no modifiers apply
     */
    public static List<String> determineAilments(LivingEntity attacker, String element) {

        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        // 1. Highest priority: alternate ailments (PoE: "Your Cold Damage inflicts Brittle instead")
        List<String> alternate = mods.getAlternateAilments(normalize(element));
        if (!alternate.isEmpty()) {
            return alternate;
        }

        // 2. Start with default ailments for this element
        List<String> base = new ArrayList<>(AilmentsRegistry.getAilmentsForElement(normalize(element)));

        // 3. Apply replacements (PoE: "Your Fire Damage can Shock instead of Ignite")
        for (int i = 0; i < base.size(); i++) {
            String replacement = mods.getReplacement(base.get(i).toLowerCase(Locale.ROOT).trim());
            if (replacement != null) {
                base.set(i, replacement);
            }
        }

        return base;
    }
}
