package com.anionianonion.elementalattackdamagecompat.ailments.higher_order;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

@FunctionalInterface
public interface StackPayloadFunction {
    Object createPayload(LivingEntity target, AilmentInstance instance);
}

