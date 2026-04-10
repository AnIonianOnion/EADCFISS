package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class BleedEffect extends DamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 5f;

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {

        inst.incrementTickCounter();
        if(inst.getTickCounter() < 20) return;
        inst.resetTickCounter();

        double speed = entity.getDeltaMovement().lengthSqr();

        float base = inst.sourceDamage * 0.70f;
        float movingBonus = speed > 0.001 ? base * 2f : 0f;

        entity.hurt(ModDamageSources.bleed((ServerLevel) entity.level()), base + movingBonus);
    }

    @Override
    public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS;
    }
}
