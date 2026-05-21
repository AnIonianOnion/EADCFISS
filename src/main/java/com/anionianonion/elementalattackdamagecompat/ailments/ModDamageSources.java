package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.api.DamageSourceBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;

public class ModDamageSources {

    public static DamageSource ignite(ServerLevel level) {
        return DamageSourceBuilder.ofDoT(level, ElementalAttackDamageCompatMod.MOD_ID, "ignite");
    }

    public static DamageSource bleed(ServerLevel level) {
        return DamageSourceBuilder.ofDoT(level, ElementalAttackDamageCompatMod.MOD_ID, "bleed");
    }

    public static DamageSource poison(ServerLevel level) {
        return DamageSourceBuilder.ofDoT(level, ElementalAttackDamageCompatMod.MOD_ID, "poison");
    }
}
