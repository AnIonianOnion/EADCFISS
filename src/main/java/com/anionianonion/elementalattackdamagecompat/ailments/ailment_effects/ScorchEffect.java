package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

public class ScorchEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        // Example: reduce armor or custom resistance attribute
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }
}
