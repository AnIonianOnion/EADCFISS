package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public class BrittleEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;

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
        return true;
    }

    @Override
    public float getBasicEffectStrength() {
        return 0;
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
    public float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
