package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class AilmentData implements IAilmentData {

    private final Map<String, AilmentInstance> ailments =
            new HashMap<>();

    @Override
    public void addAilment(String ailment, AilmentEffect effect, float sourceDamage, int duration, LivingEntity defender, IAilmentModifiers attackerMods) {
        String key = normalize(ailment);

        int extraStacks = attackerMods.getExtraStacks(key);
        boolean stackingEnabled = extraStacks > 0;

        // Create new instance
        AilmentInstance newInst = new AilmentInstance(effect, sourceDamage, duration);

        // If no existing ailment → apply immediately
        if (!ailments.containsKey(key)) {
            ailments.put(key, newInst);
            effect.onApply(defender, newInst);

            // Apply extra stacks
            for (int i = 0; i < extraStacks; i++) {
                AilmentInstance extra = new AilmentInstance(effect, sourceDamage, duration);
                ailments.put(key + "_stack_" + i, extra);
                effect.onApply(defender, extra);
            }

            return;
        }

        // Existing ailment
        AilmentInstance old = ailments.get(key);

        // STACKING MODE
        if (stackingEnabled) {
            // Add new stack without removing old
            String stackKey = key + "_stack_" + System.nanoTime();
            ailments.put(stackKey, newInst);
            effect.onApply(defender, newInst);

            // Apply extra stacks
            for (int i = 0; i < extraStacks; i++) {
                AilmentInstance extra = new AilmentInstance(effect, sourceDamage, duration);
                ailments.put(stackKey + "_extra_" + i, extra);
                effect.onApply(defender, extra);
            }

            return;
        }

        // STRONGEST-WINS MODE
        if (sourceDamage > old.sourceDamage) {
            old.onExpire(defender, old);
            ailments.put(key, newInst);
            effect.onApply(defender, newInst);
            return;
        }

        // REFRESH DURATION MODE
        if (sourceDamage == old.sourceDamage) {
            old.refresh(duration);
        }

        // weaker application does nothing
    }




    @Override
    public Map<String, AilmentInstance> getAilments() {
        return ailments;
    }

    @Override
    public void tick(LivingEntity entity) {
        Iterator<Map.Entry<String, AilmentInstance>> it = ailments.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            var inst = entry.getValue();

            // Apply effect
            inst.tickEffect(entity, inst);

            // Decrement duration
            if (inst.tickDown()) {
                inst.onExpire(entity, inst);
                it.remove(); // REQUIRED
            }
        }
    }

    private String normalize(String key) {
        return key.toLowerCase(Locale.ROOT).trim();
    }
}
