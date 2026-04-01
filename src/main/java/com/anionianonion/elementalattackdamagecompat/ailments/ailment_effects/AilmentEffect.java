package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

public interface AilmentEffect {
    void tick(LivingEntity entity, AilmentInstance instance);
    void onExpire(LivingEntity entity, AilmentInstance instance);
}
