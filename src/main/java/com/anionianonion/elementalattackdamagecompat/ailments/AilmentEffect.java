package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.EmptyAilmentEffect;
import net.minecraft.world.entity.LivingEntity;

public interface AilmentEffect {

    /**
    Tick, by default, is only used by instances of DamagingAilment to apply damage.
     */
    default void tick(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }

    /**
     Extra util method called to apply extra functions to the defender, when applying an ailment instance. An example use case would be changing the defender's attributes, based on the effect strength from the instance.
     */
    default void onApply(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }
    /**
     Extra util method called to apply extra functions to the defender, when expiring an ailment instance. An example use case would be removing attribute modifiers from the onApply method.
     */
    default void onExpire(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }

    default void onStackExpire(LivingEntity defender, AilmentInstance inst,
                              AilmentInstance.StackEntry stack, Object payload) {
        // default: do nothing
    }

    default Object createStackPayload(LivingEntity defender, AilmentInstance instance) {
        return null; // default: no payload
    }

    float getDurationInSeconds(LivingEntity livingAttackerOrCaster);

    AilmentStackingMode getStackingMode();

    default int getMaxStacks() {
        return 1;
    }

    static AilmentEffect empty() {
        return EmptyAilmentEffect.INSTANCE;
    }
}
