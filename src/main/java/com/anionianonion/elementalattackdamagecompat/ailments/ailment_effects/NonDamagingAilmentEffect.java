package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public abstract class NonDamagingAilmentEffect implements AilmentEffect {

    /**
     Subclasses may or may not need to compute effect strength and duration (Scorch, Chill, Freeze, Brittle, Shock, Sap).
     If nonDamagingAilment is Freeze: use only variableEffectDuration.
     If nonDamagingAilment is not Freeze: use only variableEffectStrength and getMaxEffectStrength to clamp value between 0 and max.
     */
    public abstract float computeVariableEffectStrength(float hitDamage, LivingEntity defender, LivingEntity attacker);
    /**
     Subclasses may or may not need to compute effect strength and duration (Scorch, Chill, Freeze, Brittle, Shock, Sap).
     If nonDamagingAilment is Freeze: use only variableEffectDuration.
     If nonDamagingAilment is not Freeze: use only variableEffectStrength and getMaxEffectStrength to clamp value between 0 and max.
     */
    protected abstract float getMaxEffectStrength(LivingEntity livingAttackerOrCaster);
    /**
     Subclasses may or may not need to compute effect strength and duration (Scorch, Chill, Freeze, Brittle, Shock, Sap).
     If nonDamagingAilment is Freeze: use only variableEffectDuration.
     If nonDamagingAilment is not Freeze: use only variableEffectStrength and getMaxEffectStrength to clamp value between 0 and max.
     */
    protected abstract float computeVariableEffectDuration(float hitDamage, LivingEntity defender, LivingEntity livingAttackerOrCaster);

    /**
     * Shared threshold logic.
     */
    protected float getAilmentThreshold(LivingEntity defender) {
        if(defender.getTags().contains("boss")) return defender.getMaxHealth() * 0.1f;
        return defender.getMaxHealth() * 0.5f;
    }
}
