package com.anionianonion.elementalattackdamagecompat.ailments.higher_order;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface StackExpireFunction {
    void onStackExpire(LivingEntity target, AilmentInstance instance, AilmentInstance.StackEntry stackEntry);
}

