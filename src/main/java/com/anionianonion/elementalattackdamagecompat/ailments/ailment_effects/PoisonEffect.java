package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class PoisonEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        float damage = inst.sourceDamage * 0.2f / 20f; // weaker than ignite

        entity.hurt(ModDamageSources.poison((ServerLevel) entity.level()), damage);
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }
}
