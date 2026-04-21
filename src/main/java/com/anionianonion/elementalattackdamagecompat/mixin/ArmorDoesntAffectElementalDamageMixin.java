package com.anionianonion.elementalattackdamagecompat.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

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