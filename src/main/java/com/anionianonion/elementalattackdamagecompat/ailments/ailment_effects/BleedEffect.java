package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class BleedEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        double speed = entity.getDeltaMovement().lengthSqr();

        float base = inst.sourceDamage * 0.70f / 20f;
        float movingBonus = speed > 0.001 ? base * 2f : 0f;

        entity.hurt(ModDamageSources.bleed((ServerLevel) entity.level()), base + movingBonus);
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }
}
