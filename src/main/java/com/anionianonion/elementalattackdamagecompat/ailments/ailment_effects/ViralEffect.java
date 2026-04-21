package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class ViralEffect extends NonDamagingAilmentEffect {
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
        return 0.5f; //multiplies enemy life by 0.5 per stack
    }

    @Override
    public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity attacker) {
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

    @Override
    public void tick(LivingEntity defender, AilmentInstance instance) {

    }

    @Override
    public void onApply(LivingEntity defender, AilmentInstance instance) {

    }


    @Override
    public void onExpire(LivingEntity defender, AilmentInstance instance) {

    }


    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return 10;
    }
}
