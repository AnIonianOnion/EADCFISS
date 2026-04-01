package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class IgniteEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {

        entity.setSharedFlagOnFire(true);
        inst.incrementTickCounter();
        if(inst.getTickCounter() < 20) return;
        inst.resetTickCounter();

        float damage = inst.sourceDamage * 0.9f;

        entity.hurt(ModDamageSources.ignite((ServerLevel) entity.level()), damage);
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {
        entity.setSharedFlagOnFire(false);
    }
}
