package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.EmptyAilmentEffect;
import net.minecraft.world.entity.LivingEntity;

public interface AilmentEffect {
    /**
    Tick, by default, is only used by instances of DamagingAilment to apply damage.
     */
    void tick(LivingEntity defender, AilmentInstance instance);

    /**
     Extra util method called to apply extra functions to the defender, when applying an ailment instance. An example use case would be changing the defender's attributes, based on the effect strength from the instance.
     */
    void onApply(LivingEntity defender, AilmentInstance instance);
    /**
     Extra util method called to apply extra functions to the defender, when expiring an ailment instance. An example use case would be removing attribute modifiers from the onApply method.
     */
    void onExpire(LivingEntity defender, AilmentInstance instance);
    float getDurationInSeconds(LivingEntity livingAttackerOrCaster);

    static AilmentEffect empty() {
        return EmptyAilmentEffect.INSTANCE;
    }
}
