package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class ScorchEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "scorch");
    }

    @Override
    public AilmentStackingMode getStackingMode() {
        return AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY;
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
        //float scorch = 50 * base * (1 + inc) / 100f;
        float scorch = 0.5f * base * AttributeHelpers.getNonDamagingAilmentEffectMultiplier(livingAttackerOrCaster, "scorch");
        return Mth.clamp(scorch, 0, maxStrength);
    }

    @Override
    public float getMaxEffectStrength(LivingEntity livingAttackerOrCaster) {
        Float maxScorchEffect = ModAttributes.getAttributeValue(livingAttackerOrCaster, String.format("%s:max_scorch_effect", ElementalAttackDamageCompatMod.MOD_ID));
        if(maxScorchEffect == null) maxScorchEffect = 0.3f;
        return maxScorchEffect;
    }

    @Override
    public float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
