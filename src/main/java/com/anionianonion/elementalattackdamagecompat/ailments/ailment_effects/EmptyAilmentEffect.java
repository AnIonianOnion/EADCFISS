package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

public final class EmptyAilmentEffect implements AilmentEffect {

    public static final EmptyAilmentEffect INSTANCE = new EmptyAilmentEffect();

    private EmptyAilmentEffect() {}

    @Override
    public void tick(LivingEntity defender, AilmentInstance instance) {
        // no-op
    }

    @Override
    public void onApply(LivingEntity defender, AilmentInstance instance) {
        // no-op
    }

    @Override
    public void onExpire(LivingEntity defender, AilmentInstance instance) {
        // no-op
    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return 0f; // or 1f if you want it to auto-expire cleanly
    }
}
