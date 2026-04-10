package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

public class ChillEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 2f;

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        int amplifier = 0; // mild slow
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, amplifier, false, false));
    }

    @Override
    public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        Float inc = AttributeHelpers.getNetIncrease(livingAttackerOrCaster, ModAttributes.getAttribute(String.format("%s:shock_duration", ElementalAttackDamageCompatMod.MOD_ID)));
        if(inc == null) inc = 0f;
        return BASE_DURATION_IN_SECONDS * (1f + inc);
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
    protected float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
