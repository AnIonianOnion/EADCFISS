package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public interface IAilmentData {

    void addAilment(String ailmentKey, AilmentInstance newInst, LivingEntity defender);

    Map<String, AilmentInstance> getAilmentsOnEntity();

    void tick(LivingEntity entity);
}
