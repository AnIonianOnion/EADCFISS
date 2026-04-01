package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Element;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class AilmentResolver {

    /**
     * Determine which ailments should be applied for a given element,
     * taking into account attacker modifiers:
     *
     * - Alternate ailments override everything
     * - Replacements override defaults
     * - Defaults are used if no modifiers apply
     */
    public static List<Ailment> determineAilments(LivingEntity attacker, Element element) {

        IAilmentModifiers mods = AilmentModifierHelper.get(attacker);

        // 1. Highest priority: alternate ailments (PoE: "Your Cold Damage inflicts Brittle instead")
        List<Ailment> alternate = mods.alternate(element);
        if (!alternate.isEmpty()) {
            return alternate;
        }

        // 2. Start with default ailments for this element
        List<Ailment> base = new ArrayList<>(DefaultAilments.get(element));

        // 3. Apply replacements (PoE: "Your Fire Damage can Shock instead of Ignite")
        for (int i = 0; i < base.size(); i++) {
            Ailment replacement = mods.replace(base.get(i));
            if (replacement != null) {
                base.set(i, replacement);
            }
        }

        return base;
    }
}
