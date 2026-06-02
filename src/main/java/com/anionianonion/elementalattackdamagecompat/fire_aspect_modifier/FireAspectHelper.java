package com.anionianonion.elementalattackdamagecompat.fire_aspect_modifier;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.List;

public class FireAspectHelper {

    public static void applyFireIgnite(LivingEntity attacker, LivingEntity target, FireSourceType type, int durationTicks) {
        CompoundTag tag = target.getPersistentData();
        tag.putUUID("fa_attacker", attacker.getUUID());
        tag.putString("fa_type", type.name());
        tag.putInt("fa_ticks", durationTicks);
        target.setRemainingFireTicks(durationTicks);
    }

    public static float computeFireDamage(LivingEntity attacker, FireSourceType type) {
        return switch (type) {
            case ATTACK_MELEE -> 10;
            case ATTACK_PROJECTILE -> 100;
            case SPELL -> 1000;
        };

    }


}
