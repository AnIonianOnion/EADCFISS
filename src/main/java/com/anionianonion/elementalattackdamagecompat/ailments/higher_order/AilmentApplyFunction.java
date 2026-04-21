package com.anionianonion.elementalattackdamagecompat.ailments.higher_order;

import net.minecraft.world.entity.LivingEntity;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;

@FunctionalInterface
public interface AilmentApplyFunction {
    void apply(LivingEntity target, AilmentInstance instance);
}
