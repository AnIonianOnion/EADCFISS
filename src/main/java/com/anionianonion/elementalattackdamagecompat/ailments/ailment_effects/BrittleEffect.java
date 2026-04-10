package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class BrittleEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;

    @Override
    public void tick(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "chill");
    }

    @Override
    public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        float threshold = getAilmentThreshold(defender);
        float ratio = hitDamage / threshold;
        float base = (float)Math.pow(ratio, 0.4);
        float maxStrength = getMaxEffectStrength(livingAttackerOrCaster);
        //maxStrength is almost the same as brittle which are both around 0.06 by default
        float brittle = 0.1f * base * AttributeHelpers.getNonDamagingAilmentEffectMultiplier(livingAttackerOrCaster, "brittle");
        return Mth.clamp(brittle, 0, maxStrength);
    }

    @Override
    public float getMaxEffectStrength(LivingEntity livingAttackerOrCaster) {
        Float maxBrittleEffect = ModAttributes.getAttributeValue(livingAttackerOrCaster, String.format("%s:max_brittle_effect", ElementalAttackDamageCompatMod.MOD_ID));
        if(maxBrittleEffect == null) maxBrittleEffect = 0.06f;
        return maxBrittleEffect;
    }

    @Override
    protected float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
