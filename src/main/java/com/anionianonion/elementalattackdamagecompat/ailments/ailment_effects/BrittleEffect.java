package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

public class BrittleEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        // Example: add crit vulnerability attribute
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }
}
