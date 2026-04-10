package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

public interface AilmentEffect {

    //Component getTranslatedName();
    void tick(LivingEntity entity, AilmentInstance instance);
    void onExpireExtraFunction(LivingEntity entity, AilmentInstance instance);
    float getDurationInSeconds(LivingEntity livingAttackerOrCaster);
}
