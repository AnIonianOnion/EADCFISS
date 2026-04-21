package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public interface IAilmentData {

    void addAilment(String ailment, AilmentEffect effect, float sourceDamage, int duration, LivingEntity defender, IAilmentModifiers attackerMods);

    Map<String, AilmentInstance> getAilments();

    void tick(LivingEntity entity);
}
