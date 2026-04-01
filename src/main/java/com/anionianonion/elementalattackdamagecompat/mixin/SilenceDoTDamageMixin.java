package com.anionianonion.elementalattackdamagecompat.mixin;

import com.anionianonion.elementalattackdamagecompat.ModDamageTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class SilenceDoTDamageMixin {

    @Inject(method = "playHurtSound", at = @At("HEAD"), cancellable = true)
    private void suppressDotHurtSound(DamageSource source, CallbackInfo ci) {
        if (source.is(ModDamageTags.DOT_DAMAGE)) {
            ci.cancel(); // suppress hurt sound
        }
    }
}
