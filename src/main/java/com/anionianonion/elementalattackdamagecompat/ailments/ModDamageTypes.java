package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {

    public static final ResourceKey<DamageType> IGNITE =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(String.format("%s:ignite", ElementalAttackDamageCompatMod.MOD_ID)));

    public static final ResourceKey<DamageType> BLEED =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(String.format("%s:bleed", ElementalAttackDamageCompatMod.MOD_ID)));

    public static final ResourceKey<DamageType> POISON =
            ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse(String.format("%s:poison", ElementalAttackDamageCompatMod.MOD_ID)));
}
