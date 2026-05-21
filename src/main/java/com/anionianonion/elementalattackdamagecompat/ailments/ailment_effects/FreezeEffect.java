package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class FreezeEffect extends NonDamagingAilmentEffect {

    @Override
    public void tick(LivingEntity defender, AilmentInstance inst) {
        defender.setDeltaMovement(0, defender.getDeltaMovement().y, 0);

        if (defender instanceof Mob mob) {
            mob.getNavigation().stop();
            mob.setNoAi(true);


        }
    }

    @Override
    public void onExpire(LivingEntity defender, AilmentInstance instance) {
        if (defender instanceof Mob mob) {
            mob.setNoAi(false);

            LivingEntity oldTarget = instance.storedTarget;
            if (oldTarget != null && oldTarget.isAlive()) {
                mob.setTarget(oldTarget);
                mob.getNavigation().moveTo(oldTarget, 1.0);
                mob.setAggressive(true);
            }

            instance.storedTarget = null;
        }
    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return 0;
        //cannot set it here
    }

    @Override
    public AilmentStackingMode getStackingMode() {
        return AilmentStackingMode.STRONGEST_DURATION;
    }

    @Override
    protected boolean usesVaryingEffectDuration() {
        return true;
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
        //0.06 seconds of freeze per 1% of ailmentThreshold met.
        //discard freeze if it is less than 0.3s.
        //getAilmentThreshold returns a decimal, so we convert it to a percentage by multiplying by 100.
        //float duration = 0.06f * (getAilmentThreshold(defender) * 100f);
        float duration = 6 * getAilmentThreshold(defender) * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "freeze");
        if(duration < 0.3f) return 0f; //discard
        return Math.min(duration, 3); //max of 3s.

    }
}
