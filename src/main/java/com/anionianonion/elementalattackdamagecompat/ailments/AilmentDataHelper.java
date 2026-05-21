package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.capability.AilmentDataCapability;
import com.anionianonion.elementalattackdamagecompat.util.ModUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentDataHelper {

    public static Optional<IAilmentData> getOptional(LivingEntity entity) {
        return entity.getCapability(AilmentDataCapability.INSTANCE).resolve();
    }

    public static IAilmentData get(LivingEntity entity) {
        return getOptional(entity).orElseThrow(() ->
                new IllegalStateException("Missing AilmentData capability"));
    }

    public static AilmentInstance getAilment(LivingEntity entity, String ailmentKey) {
        String normalized = normalize(ailmentKey);
        return getOptional(entity)
                .map(cap -> cap.getAilmentsOnEntity().get(normalized))
                .orElse(null);
    }
}