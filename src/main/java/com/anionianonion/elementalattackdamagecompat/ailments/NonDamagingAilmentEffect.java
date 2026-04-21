package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

public abstract class NonDamagingAilmentEffect implements AilmentEffect {

    private final boolean isUsingVaryingEffectDuration;
    private final boolean isUsingVaryingEffectStrength;

    protected NonDamagingAilmentEffect() {
        this.isUsingVaryingEffectStrength = usesVaryingEffectStrength();
        this.isUsingVaryingEffectDuration = usesVaryingEffectDuration();
    }

    protected abstract boolean usesVaryingEffectDuration();
    protected abstract boolean usesVaryingEffectStrength();

    public boolean isUsingVaryingEffectDuration() {
        return isUsingVaryingEffectDuration;
    }
    public boolean isUsingVaryingEffectStrength() {
        return isUsingVaryingEffectStrength;
    }


    /**
    This method is only used if effectStrength is a constant.
     */
    public abstract float getBasicEffectStrength();
    public abstract float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity attacker);
    protected abstract float getMaxEffectStrength(LivingEntity livingAttackerOrCaster);
    public abstract float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster);


    /**
     * Shared threshold logic.
     */
    protected float getAilmentThreshold(LivingEntity defender) {
        if(defender.getTags().contains("boss")) return defender.getMaxHealth() * 0.1f;
        return defender.getMaxHealth() * 0.5f;
    }
}
