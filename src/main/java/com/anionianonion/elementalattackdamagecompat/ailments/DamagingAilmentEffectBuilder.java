package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ailments.higher_order.AilmentApplyFunction;
import com.anionianonion.elementalattackdamagecompat.ailments.higher_order.AilmentExpireFunction;
import com.anionianonion.elementalattackdamagecompat.ailments.higher_order.AilmentTickFunction;
import net.minecraft.world.entity.LivingEntity;

public class DamagingAilmentEffectBuilder {

    private String namespace;
    private String id;
    private float baseDurationInSeconds;
    private float ratioOfHitDamageToBecomeDoT;
    private AilmentTickFunction onTickFunc = (defender, instance) -> {};
    private AilmentApplyFunction onApplyFunc = (defender, instance) -> {};
    private AilmentExpireFunction onExpireFunc = (defender, instance) -> {};
    private AilmentStackingMode stackingMode = AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE;
    private int maxStacks = 1;

    /**
        this float will be multiplied by the ailment_instance#hit_damage in order to determine how much damage to deal as DoT per second.
     */
    public DamagingAilmentEffectBuilder setRatioOfHitDamageToBecomeDoT(float ratio) {
        this.ratioOfHitDamageToBecomeDoT = ratio;
        return this;
    }

    public DamagingAilmentEffectBuilder setNamespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DamagingAilmentEffectBuilder setId(String id) {
        this.id = id;
        return this;
    }


    public DamagingAilmentEffectBuilder setBaseDurationInSeconds(float baseDurationInSeconds) {
        this.baseDurationInSeconds = baseDurationInSeconds;
        return this;
    }

    public DamagingAilmentEffectBuilder stackingMode(AilmentStackingMode mode) {
        this.stackingMode = mode;
        return this;
    }

    public DamagingAilmentEffectBuilder setMaxStacks(int maxStacks) {
        this.maxStacks = maxStacks;
        return this;
    }


    //----------First Order Functions----------
    public DamagingAilmentEffectBuilder onTick(AilmentTickFunction func) {
        this.onTickFunc = func;
        return this;
    }
    public DamagingAilmentEffectBuilder onApply(AilmentApplyFunction func) {
        this.onApplyFunc = func;
        return this;
    }
    public DamagingAilmentEffectBuilder onExpire(AilmentExpireFunction func) {
        this.onExpireFunc = func;
        return this;
    }

    //----------Result----------
    public DamagingAilmentEffect build() {
        if(id != null && namespace != null) return new DamagingAilmentEffect() {
            @Override
            protected int frequencyInTicks() {
                return 20;
            }

            @Override
            public void tick(LivingEntity defender, AilmentInstance instance) {
                onTickFunc.tick(defender, instance);
            }

            @Override
            public void onFirstApplication(LivingEntity defender, AilmentInstance instance) {
                onApplyFunc.apply(defender, instance);
            }

            @Override
            public void onLastExpiration(LivingEntity defender, AilmentInstance instance) {
                onExpireFunc.expire(defender, instance);
            }

            @Override
            public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
                return baseDurationInSeconds * AttributeHelpers.getDamagingAilmentDurationMultiplier(livingAttackerOrCaster, id);
            }

            @Override
            public AilmentStackingMode getStackingMode() {
                return stackingMode;
            }

            @Override
            public int getDefaultMaxStacks() {
                return maxStacks;
            }
        };
        return null;
    }
}
