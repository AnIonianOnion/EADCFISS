package com.anionianonion.elementalattackdamagecompat.api;

import com.anionianonion.elementalattackdamagecompat.ailments.DoTDamageSource;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public class DamageSourceBuilder {

    private final ServerLevel level;
    private String namespace;
    private String id;
    private boolean isDoT;

    private DamageSourceBuilder(ServerLevel level) {
        this.level = level;
    }

    public static DamageSourceBuilder create(ServerLevel level) {
        return new DamageSourceBuilder(level);
    }

    public DamageSourceBuilder namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    public DamageSourceBuilder id(String id) {
        this.id = id;
        return this;
    }

    public DamageSourceBuilder dot() {
        this.isDoT = true;
        return this;
    }

    public DamageSource build() {
        if (namespace == null || id == null) {
            throw new IllegalStateException("Both namespace and id must be set before building a DamageSource.");
        }

        ResourceLocation rl = ResourceLocation.fromNamespaceAndPath(namespace, id);

        ResourceKey<DamageType> key =
                ResourceKey.create(Registries.DAMAGE_TYPE, rl);

        Holder<DamageType> holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(key);

        if(isDoT) return new DoTDamageSource(holder);
        return new DamageSource(holder);
    }

    public static DamageSourceBuilder of(ServerLevel level, String namespace, String id) {
        return create(level).namespace(namespace).id(id);
    }

}
