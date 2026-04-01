package com.anionianonion.elementalattackdamagecompat.mixin;

import com.anionianonion.elementalattackdamagecompat.ailments.ModDamageTypes;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ArmorDoesntAffectElementalDamageMixin {

    /*
    @Inject(method = "getDamageAfterArmorAbsorb", at = @At("HEAD"), cancellable = true)
    private void armorDoesntAffectElementalDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Float> cir) {
        if(damageSource instanceof SpellDamageSource spellDamageSource && !spellDamageSource.spell().getSchoolType().getId().getPath().equals("physical"))
            cir.setReturnValue(amount);
    }

     */
}