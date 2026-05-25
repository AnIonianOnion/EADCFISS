package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.higher_order.*;
import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class NonDamagingAilmentEffectBuilder {

    private String namespace;
    private String id;
    private String altForId;
    private int baseDurationInSeconds;
    private float effectStrength;
    private boolean doesVaryEffectStrength;
    private boolean doesVaryEffectDuration;
    private float effectCoefficient, durationCoefficient;
    private boolean discardIfBelowMinimumEffectStrength;
    private boolean discardIfBelowMinDuration;
    private float minimumEffectStrengthForKeeping;
    private float minimumDurationForKeeping;
    private float maxDuration;
    private AilmentTickFunction onTickFunc = (defender, instance) -> {};
    private AilmentApplyFunction onApplyFunc = (defender, instance) -> {};
    private AilmentExpireFunction onExpireFunc = (defender, instance) -> {};
    private StackPayloadFunction createStackPayloadFunc = (defender, instance) -> null;
    private StackExpireFunction onStackExpireFunc = (defender, instance, stack, payload) -> {};
    private AilmentStackingMode stackingMode = AilmentStackingMode.STRONGEST_WINS;
    private int maxStacks = 1;

    //----------Basic---------
    public NonDamagingAilmentEffectBuilder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setId(String id) {
        this.id = id;
        return this;
    }
    
    //----------Effect Strength----------
    public NonDamagingAilmentEffectBuilder doesVaryEffectStrength(boolean doesVaryEffectStrength) {
        this.doesVaryEffectStrength = doesVaryEffectStrength;
        return this;
    }
    
    public NonDamagingAilmentEffectBuilder setEffectStrength(float strength) {
        if(this.doesVaryEffectStrength) throw new IllegalStateException("Effect cannot have a constant strength when you let the effect vary its strength, which means the effect strength is determined by the specific instance of elemental damage from the hit itself.");
        this.effectStrength = strength;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setEffectStrengthCoefficient(float coefficient) {
        if(!this.doesVaryEffectStrength) throw new IllegalStateException("Effect cannot have a effect strength coefficient when you don't let the effect vary its strength, which means the effect strength is determined by the specific instance of elemental damage from the hit itself. This coefficient is used in determining the effect's strength.");
        this.effectCoefficient = coefficient;
        return this;
    }
    
    public NonDamagingAilmentEffectBuilder setIfDiscardIfBelowMinimumEffectStrength(boolean discardIfBelowMinimumEffectStrength) {
        if(!this.doesVaryEffectStrength) throw new IllegalStateException("Effect cannot discard below minimum strength when you don't let the effect vary its strength, which means the effect strength is determined by the specific instance of elemental damage from the hit itself.");
        this.discardIfBelowMinimumEffectStrength = discardIfBelowMinimumEffectStrength;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setMinEffectStrengthToKeep(float minEffectStrength) {
        if(!this.doesVaryEffectStrength) throw new IllegalStateException("Effect cannot discard below minimum strength when you don't let the effect vary its strength, which means the effect strength is determined by the specific instance of elemental damage from the hit itself.");
        if(minEffectStrength < 0f) minEffectStrength = 0f;
        this.minimumEffectStrengthForKeeping = minEffectStrength;
        return this;
    }

    //----------Effect Duration----------
    public NonDamagingAilmentEffectBuilder doesVaryEffectDuration(boolean doesVaryEffectDuration) {
        this.doesVaryEffectDuration = doesVaryEffectDuration;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setBaseDurationInSeconds(int baseDurationInSeconds) {
        if(this.doesVaryEffectDuration) throw new IllegalStateException("Effect cannot have a base duration when you let the effect vary its duration, which means the effect duration is determined by the specific instance of elemental damage from the hit itself.");
        else this.baseDurationInSeconds = baseDurationInSeconds;
        return this;
    }
    
    public NonDamagingAilmentEffectBuilder setDurationCoefficient(float coefficient) {
        if(!this.doesVaryEffectDuration) throw new IllegalStateException("Effect cannot have a effect duration coefficient when you don't let the effect vary its duration, which means the effect duration is determined by the specific instance of elemental damage from the hit itself. This coefficient is used in determining the effect's duration.");
        this.durationCoefficient = coefficient;
        return this;
    }
    
    public NonDamagingAilmentEffectBuilder setIfDiscardIfBelowMinimumDuration(boolean discardIfBelowMinimumDuration) {
        this.discardIfBelowMinDuration = discardIfBelowMinimumDuration;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setMinDurationToKeep(float minimumDurationInSeconds) {
        if(!this.doesVaryEffectDuration) throw new IllegalStateException("Effect cannot discard below minimum duration when you don't let the effect vary its duration, which means the effect duration is determined by the specific instance of elemental damage from the hit itself.");
        if(minimumDurationInSeconds < 0f) minimumDurationInSeconds = 0f;
        this.minimumDurationForKeeping = minimumDurationInSeconds;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setMaxDuration(float maxDuration) {
        if(!this.doesVaryEffectDuration) throw new IllegalStateException("Effect cannot have a max duration when you don't let the effect vary its duration, which means the effect duration is determined by the specific instance of elemental damage from the hit itself.");
        this.maxDuration = maxDuration;
        return this;
    }

    public NonDamagingAilmentEffectBuilder stackingMode(AilmentStackingMode mode) {
        this.stackingMode = mode;
        return this;
    }

    public NonDamagingAilmentEffectBuilder setMaxStacks(int maxStacks) {
        this.maxStacks = maxStacks;
        return this;
    }

    //----------First Order Functions----------
    public NonDamagingAilmentEffectBuilder onTick(AilmentTickFunction func) {
        this.onTickFunc = func;
        return this;
    }
    public NonDamagingAilmentEffectBuilder onApply(AilmentApplyFunction func) {
        this.onApplyFunc = func;
        return this;
    }
    public NonDamagingAilmentEffectBuilder onExpire(AilmentExpireFunction func) {
        this.onExpireFunc = func;
        return this;
    }
    public NonDamagingAilmentEffectBuilder createStackPayload(StackPayloadFunction func) {
        this.createStackPayloadFunc = func;
        return this;
    }
    public NonDamagingAilmentEffectBuilder onStackExpire(StackExpireFunction func) {
        this.onStackExpireFunc = func;
        return this;
    }


    //----------Result----------
    public NonDamagingAilmentEffect build() {
        if(namespace != null && id != null) {
            return new NonDamagingAilmentEffect() {

                @Override
                public void tick(LivingEntity defender, AilmentInstance instance) {
                    onTickFunc.tick(defender, instance);
                }

                @Override
                public void onApply(LivingEntity defender, AilmentInstance instance) {
                    onApplyFunc.apply(defender, instance);
                }

                @Override
                public void onExpire(LivingEntity defender, AilmentInstance instance) {
                    onExpireFunc.expire(defender, instance);
                }

                @Override
                public void onStackExpire(LivingEntity defender, AilmentInstance inst, AilmentInstance.StackEntry stack, Object payload) {
                    onStackExpireFunc.onStackExpire(defender, inst, stack, payload);
                }

                @Override
                public Object createStackPayload(LivingEntity defender, AilmentInstance instance) {
                    return createStackPayloadFunc.createPayload(defender, instance);
                }

                @Override
                public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
                    return baseDurationInSeconds * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, id);
                }

                @Override
                public AilmentStackingMode getStackingMode() {
                    return stackingMode;
                }

                @Override
                public int getDefaultMaxStacks() {
                    return maxStacks;
                }

                @Override
                protected boolean usesVaryingEffectDuration() {
                    return doesVaryEffectDuration;
                }

                @Override
                protected boolean usesVaryingEffectStrength() {
                    return doesVaryEffectStrength;
                }

                @Override
                public float getBasicEffectStrength() {
                    return effectStrength;
                }

                @Override
                public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {

                    float threshold = getAilmentThreshold(defender);
                    float ratio = hitDamage / threshold;
                    float base = (float)Math.pow(ratio, 0.4);
                    float maxStrength = getMaxEffectStrength(livingAttackerOrCaster);

                    float effect = effectCoefficient * base * AttributeHelpers.getNonDamagingAilmentEffectMultiplier(livingAttackerOrCaster, id);
                    if(effect < minimumEffectStrengthForKeeping && discardIfBelowMinimumEffectStrength) return 0f;
                    return Mth.clamp(effect, 0, maxStrength);
                }

                @Override
                protected float getMaxEffectStrength(LivingEntity livingAttackerOrCaster) {

                    Float maxEffectStrength = ModAttributes.getAttributeValue(livingAttackerOrCaster, String.format("%s:max_%s_effect", namespace, id));
                    if(maxEffectStrength == null) maxEffectStrength = 0f;
                    return maxEffectStrength;
                }

                @Override
                public float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
                    float duration = durationCoefficient * getAilmentThreshold(defender);
                    if(duration < minimumDurationForKeeping && discardIfBelowMinDuration) return 0f;
                    return Mth.clamp(duration, 0, maxDuration);
                }
            };
        }
        return null;
    }

}
