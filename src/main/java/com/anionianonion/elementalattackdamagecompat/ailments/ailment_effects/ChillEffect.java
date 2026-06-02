package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class ChillEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 2f;

    @Override
    public void tick(LivingEntity defender, AilmentInstance inst) {
        int amplifier = 0; // mild slow
        defender.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, amplifier, false, false));
    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "chill");
    }

    @Override
    public List<AilmentStackingMode> getStackingModes() {
        return new ArrayList<>(List.of(AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY, AilmentStackingMode.REFRESH_DURATION));
    }
    @Override
    protected boolean usesVaryingEffectDuration() {
        return false;
    }

    @Override
    protected boolean usesVaryingEffectStrength() {
        return false;
    }

    @Override
    public float getBasicEffectStrength() {
        return 0;
    }

    @Override
    public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }

    @Override
    protected float getMaxEffectStrength(LivingEntity livingAttackerOrCaster) {
        return 0;
    }

    @Override
    public float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
