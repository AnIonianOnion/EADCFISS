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
}
