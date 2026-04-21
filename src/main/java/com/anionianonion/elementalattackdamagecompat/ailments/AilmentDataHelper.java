package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.capability.AilmentDataCapability;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class AilmentDataHelper {

    public static Optional<IAilmentData> getOptional(LivingEntity entity) {
        return entity.getCapability(AilmentDataCapability.INSTANCE).resolve();
    }

    public static IAilmentData get(LivingEntity entity) {
        return getOptional(entity).orElseThrow(() ->
                new IllegalStateException("Missing AilmentData capability"));
    }

    public static AilmentInstance getAilment(LivingEntity entity, String ailment) {
        return getOptional(entity)
                .map(cap -> cap.getAilments().get(ailment))
                .orElse(null);
    }
}

//todo: fix ailments not applying when I switched from enums to Strings