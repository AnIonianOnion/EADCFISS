package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class FreezeEffect implements AilmentEffect {

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        entity.setDeltaMovement(0, 0, 0);

        if (entity instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setNoAi(true);
        }
    }

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }
}
