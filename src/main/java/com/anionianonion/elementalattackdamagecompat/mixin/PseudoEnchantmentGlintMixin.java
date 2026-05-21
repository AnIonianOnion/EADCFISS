package com.anionianonion.elementalattackdamagecompat.mixin;

import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.PseudoEnchantmentHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class PseudoEnchantmentGlintMixin {

    //mixin is working as intended
    @Inject(method = "isEnchanted", at = @At("HEAD"), cancellable = true)
    private void hasGlint(CallbackInfoReturnable<Boolean> cir) {
        ItemStack stack = (ItemStack)(Object)this;

        CompoundTag tag = stack.getOrCreateTag();
        boolean hasPseudoEnchants = tag.contains(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID);

        //do not shortcut this by returning hasPseudoEnchants,
        // which will cause the original isEnchanted method to return false even if it has real enchants.
        if (hasPseudoEnchants) {
            cir.setReturnValue(true);
        }
    }
}
