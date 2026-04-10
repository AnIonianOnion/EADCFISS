package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public class SapEffect extends NonDamagingAilmentEffect {

    private static final float BASE_DURATION_IN_SECONDS = 4f;
    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {
        // Example: reduce attack damage attribute
    }

    @Override
    public void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return BASE_DURATION_IN_SECONDS * AttributeHelpers.getNonDamagingAilmentDurationMultiplier(livingAttackerOrCaster, "sap");
    }

    @Override
    public float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        float threshold = getAilmentThreshold(defender);
        float ratio = hitDamage / threshold;
        float base = (float)Math.pow(ratio, 0.4);
        float maxStrength = getMaxEffectStrength(livingAttackerOrCaster);

        float sap = 1f / 3f * base * AttributeHelpers.getNonDamagingAilmentEffectMultiplier(livingAttackerOrCaster, "sap");
        if(sap < 0.02f) return 0; //discard sap unless it meets min threshold of 2%.
        return Math.min(sap, maxStrength);
    }

    @Override
    protected float getMaxEffectStrength(LivingEntity livingAttackerOrCaster) {
        Float maxSapEffect = ModAttributes.getAttributeValue(livingAttackerOrCaster, String.format("%s:max_sap_effect", ElementalAttackDamageCompatMod.MOD_ID));
        if(maxSapEffect == null) maxSapEffect = 0.20f;
        return maxSapEffect;
    }

    @Override
    protected float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster) {
        return 0;
    }
}
