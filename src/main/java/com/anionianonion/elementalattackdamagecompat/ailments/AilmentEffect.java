package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.EmptyAilmentEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;

public interface AilmentEffect {

    /**
    Tick, by default, is only used by instances of DamagingAilment to apply damage.
     */
    default void tick(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }

    /**
    Whenever an AilmentInstance is first added, and doesn't trigger on subsequent stacks until they all expire.
     */
    default void onFirstApplication(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }
    /**
     Whenever an AilmentInstance all expires.
     */
    default void onLastExpiration(LivingEntity defender, AilmentInstance instance) {
        //default: do nothing
    }

    /**
    Whenever a stack of an AilmentInstance expires.
     */
    default void onStackExpire(LivingEntity defender, AilmentInstance inst,
                              AilmentInstance.StackEntry stack) {
        // default: do nothing
    }

    /**
     Whenever a stack of an AilmentInstance is added. An example use case would be changing the defender's attributes, based on the effect strength from the instance, and returning the attribute modifier's UUID to be removed later.
     */
    default Object onStackApply(LivingEntity defender, AilmentInstance instance) {
        return null; // default: no payload
    }

    float getDurationInSeconds(LivingEntity livingAttackerOrCaster);

    List<AilmentStackingMode> getStackingModes();

    default int getDefaultMaxStacks() {
        return 1;
    }

    static AilmentEffect empty() {
        return EmptyAilmentEffect.INSTANCE;
    }
}
