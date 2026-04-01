package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageSources {

    private static Holder<DamageType> get(ServerLevel level, ResourceKey<DamageType> key) {
        return level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);
    }

    public static DamageSource ignite(ServerLevel level) {
        return new DamageSource(get(level, ModDamageTypes.IGNITE));
    }

    public static DamageSource bleed(ServerLevel level) {
        return new DamageSource(get(level, ModDamageTypes.BLEED));
    }

    public static DamageSource poison(ServerLevel level) {
        return new DamageSource(get(level, ModDamageTypes.POISON));
    }
}
