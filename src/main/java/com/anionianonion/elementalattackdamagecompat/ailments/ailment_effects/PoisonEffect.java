package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.DamagingAilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class PoisonEffect extends DamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;

    @Override
    public void tick(LivingEntity defender, AilmentInstance inst) {

        inst.incrementTickCounter();
        if(inst.isntTimeToDealDamage()) return;
        inst.resetTickCounter();

        float damage = inst.getTotalDamage() * 0.2f;

        defender.hurt(ModDamageSources.poison((ServerLevel) defender.level()), damage);
    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS;
    }

    @Override
    public List<AilmentStackingMode> getStackingModes() {
        return new ArrayList<>(List.of(AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE));
    }

    @Override
    public int getDefaultMaxStacks() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected int frequencyInTicks() {
        return 20;
    }
}
