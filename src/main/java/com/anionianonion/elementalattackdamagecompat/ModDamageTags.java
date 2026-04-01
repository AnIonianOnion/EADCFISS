package com.anionianonion.elementalattackdamagecompat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTags {

    public static final TagKey<DamageType> DOT_DAMAGE =
            TagKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath("elementalattackdamagecompat", "is_dot"));
}
