package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.DamagingAilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class IgniteEffect extends DamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;

    @Override
    public void tick(LivingEntity defender, AilmentInstance inst) {

        inst.incrementTickCounter();
        if(inst.getTickCounter() < 20) return;
        inst.resetTickCounter();

        float damage = inst.sourceDamage * 0.9f;

        defender.hurt(ModDamageSources.ignite((ServerLevel) defender.level()), damage);
    }

    @Override
    public void onApply(LivingEntity defender, AilmentInstance instance) {

    }

    @Override
    public void onExpire(LivingEntity defender, AilmentInstance instance) {

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "ignite");
    }
}
