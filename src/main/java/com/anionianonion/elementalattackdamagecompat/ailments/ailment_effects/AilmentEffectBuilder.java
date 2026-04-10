package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;

public class AilmentEffectBuilder {

    private float baseDurationInSeconds;
    private boolean isDamagingAilment;
    private boolean doesVaryEffectStrength;
    private boolean doesVaryEffectDuration;
    private float ratioOfHitDamageToBecomeDoT;

    /**
    This method is recommended to be used first. If isDamagingAilment is false, look at NDAE section. if true, look at DAE.
     */
    public AilmentEffectBuilder setIsDamagingAilment(boolean isDamagingAilment) {
        this.isDamagingAilment = isDamagingAilment;
        return this;
    }

    //-------------------------------NDAE-------------------------------
    public AilmentEffectBuilder doesVaryEffectStrength(boolean doesVaryEffectStrength) {
        if(this.isDamagingAilment) throw new IllegalStateException("Damaging ailments do not have an effect strength. " +
                "\nRelated: Use setRatioOfHitDamageToBecomeDoT in order to modify how much of the hit damage becomes a DoT.");

        this.doesVaryEffectStrength = doesVaryEffectStrength;
        return this;
    }

    public AilmentEffectBuilder doesVaryEffectDuration(boolean doesVaryEffectDuration) {
        if(this.isDamagingAilment) throw new IllegalStateException("Damaging ailments do not have a variable effect duration (it may not have an ailment duration determined by the amount of damage in the hit.)");
        else if(this.baseDurationInSeconds != 0f) throw new IllegalStateException("Ailments may not have a base duration, and a variable ailment duration at the same time.");

        this.doesVaryEffectDuration = doesVaryEffectDuration;
        return this;
    }

    //-------------------------------DAE-------------------------------
    /**
        this float will be multiplied by the ailment_instance#hit_damage in order to determine how much damage to deal as DoT per second.
     */
    public AilmentEffectBuilder setRatioOfHitDamageToBecomeDoT(float ratio) {
        if(!this.isDamagingAilment) throw new IllegalStateException("Non-damaging ailments may not use a multiplier for converting a portion of the hit damage to be reapplied as DoT.");

        this.ratioOfHitDamageToBecomeDoT = ratio;
        return this;
    }

    //-------------------------------SHARED-------------------------------
    public AilmentEffectBuilder setBaseDurationInSeconds(float baseDurationInSeconds) {
        if(this.doesVaryEffectDuration) throw new IllegalStateException("Ailment cannot have a base duration when doesVaryEffectDuration is true, which means the effect duration is determined by the hit itself.");
        else this.baseDurationInSeconds = baseDurationInSeconds;
        return this;
    }


    public AilmentEffect build() {
        if(isDamagingAilment) return new DamagingAilmentEffect() {
            @Override
            public void tick(LivingEntity entity, AilmentInstance instance) {

            }

            @Override
            public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

            }

            @Override
            public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
                return 0;
            }
        };
        else return new NonDamagingAilmentEffect() {
            @Override
            public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity attacker) {
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

            @Override
            public void tick(LivingEntity entity, AilmentInstance instance) {

            }

            @Override
            public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

            }

            @Override
            public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
                return 0;
            }
        };
    }
}
