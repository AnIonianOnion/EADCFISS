package com.anionianonion.elementalattackdamagecompat.mixin;

import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DamageSources.class)
public class DamageSourcesMixin {

    @Inject(at = @At(value = "HEAD"), method = "getResist", remap = false, cancellable = true)
    private static void disableDoubleDipResist(LivingEntity entity, SchoolType damageSchool, CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(1f);
    }
}
