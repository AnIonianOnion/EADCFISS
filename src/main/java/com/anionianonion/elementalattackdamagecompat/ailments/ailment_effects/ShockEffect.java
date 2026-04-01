package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class ShockEffect implements AilmentEffect {

    private static final UUID SHOCK_UUID = UUID.fromString("e4b1c0f0-1f3b-4b1a-9c2a-123456789abc");

    @Override
    public void onExpire(LivingEntity entity, AilmentInstance instance) {

    }

    @Override
    public void tick(LivingEntity entity, AilmentInstance inst) {

        var attr = entity.getAttribute(ModAttributes.SHOCKED_EXTRA_DAMAGE_TAKEN_MULTIPLIER.get());
        if (attr == null) return;

        float effect = computeShockStrength(inst.sourceDamage, entity, entity.getLastAttacker());


        // +20% damage taken (example)
        AttributeModifier mod = new AttributeModifier(
                SHOCK_UUID,
                "shock_increased_damage_taken",
                effect,
                AttributeModifier.Operation.ADDITION
        );

        if (!attr.hasModifier(mod)) {
            attr.addTransientModifier(mod);
        }
    }

    private static final float BASE_MAX_EFFECT = 0.50f; // 50%
    private static final float BASE_DURATION_SECONDS = 2f;

    private float getMaxEffect(LivingEntity attacker) {
        Float inc = ModAttributes.getAttributeValue(attacker, String.format("%s:increased_shock_effect", ElementalAttackDamageCompatMod.MOD_ID));
        return BASE_MAX_EFFECT * (1f + inc);
    }

    public float getDurationSeconds(LivingEntity attacker) {
        Float inc = ModAttributes.getAttributeValue(attacker, String.format("%s:increased_shock_duration", ElementalAttackDamageCompatMod.MOD_ID));
        return BASE_DURATION_SECONDS * (1f + inc);
    }

    public float computeShockStrength(float hitDamage, LivingEntity defender, LivingEntity attacker) {
        float threshold = defender.getMaxHealth() * 0.5f;
        float ratio = hitDamage / threshold;

        float base = (float)Math.pow(ratio, 0.4);
        Float inc = ModAttributes.getAttributeValue(attacker, String.format("%s:increased_shock_effect", ElementalAttackDamageCompatMod.MOD_ID));
        if(inc == null) inc = 0f;

        float shock = 0.5f * base * (1f + inc);
        return Mth.clamp(shock, 0f, getMaxEffect(attacker));
    }

}
