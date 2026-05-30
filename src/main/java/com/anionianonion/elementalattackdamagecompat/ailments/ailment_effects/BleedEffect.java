package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.DamagingAilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageSources;
import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class BleedEffect extends DamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 5f;

    @Override
    public void tick(LivingEntity defender, AilmentInstance inst) {

        inst.incrementTickCounter();
        if(inst.isntTimeToDealDamage()) return;
        inst.resetTickCounter();

        double speed = defender.getDeltaMovement().lengthSqr();

        float base = inst.getTotalDamage() * 0.70f;
        float movingBonus = speed > 0.001 ? base * 2f : 0f;

        defender.hurt(ModDamageSources.bleed((ServerLevel) defender.level()), base + movingBonus);
        System.out.println("Bleed tick on " + defender + " from instance " + inst);

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "bleed");
    }

    @Override
    public AilmentStackingMode getStackingMode() {
        return AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE;
    }

    @Override
    protected int frequencyInTicks() {
        return 20; // / (1 + faster);
    }
}
