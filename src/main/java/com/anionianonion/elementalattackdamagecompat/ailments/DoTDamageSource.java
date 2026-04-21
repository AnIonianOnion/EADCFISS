package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class DoTDamageSource extends DamageSource {

    public DoTDamageSource(Holder<DamageType> typeHolder, @Nullable Entity directEntity, @Nullable Entity attacker, @Nullable Vec3 damageSourcePosition) {
        super(typeHolder, directEntity, attacker, damageSourcePosition);
    }

    public DoTDamageSource(Holder<DamageType> typeHolder) {
        super(typeHolder);
    }

    public boolean isDot() {
        return true;
    }
}
