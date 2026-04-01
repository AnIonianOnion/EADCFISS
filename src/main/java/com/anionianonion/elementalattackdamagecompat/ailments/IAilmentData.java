package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.AilmentEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public interface IAilmentData {

    void addAilment(Ailment ailment, AilmentEffect effect, float sourceDamage, int duration);

    Map<Ailment, AilmentInstance> getAilments();

    void tick(LivingEntity entity);
}
