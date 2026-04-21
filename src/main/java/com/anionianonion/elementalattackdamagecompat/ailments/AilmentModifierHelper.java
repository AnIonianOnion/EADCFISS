package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.capability.AilmentModifierCapability;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class AilmentModifierHelper {

    /**
     * Get the IAilmentModifiers capability from an entity.
     */
    public static Optional<IAilmentModifiers> getOptional(LivingEntity entity) {
        return entity.getCapability(AilmentModifierCapability.INSTANCE).resolve();
    }

    /**
     * Get the IAilmentModifiers capability, or a default no-op implementation.
     */
    public static IAilmentModifiers get(LivingEntity entity) {
        return getOptional(entity).orElse(IAilmentModifiers.EMPTY);
    }
}
