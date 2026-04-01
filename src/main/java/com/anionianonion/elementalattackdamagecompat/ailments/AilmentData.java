package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.AilmentEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

public class AilmentData implements IAilmentData {

    private final Map<Ailment, AilmentInstance> ailments =
            new EnumMap<>(Ailment.class);

    @Override
    public void addAilment(Ailment ailment, AilmentEffect effect, float sourceDamage, int duration) {
        ailments.put(ailment, new AilmentInstance(effect, sourceDamage, duration));
    }

    @Override
    public Map<Ailment, AilmentInstance> getAilments() {
        return ailments;
    }

    @Override
    public void tick(LivingEntity entity) {
        Iterator<Map.Entry<Ailment, AilmentInstance>> it = ailments.entrySet().iterator();

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
}
