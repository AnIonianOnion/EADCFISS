package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AilmentInstance {
    public final AilmentEffect effect;
    public float sourceDamage;
    private int initialDuration;
    private int duration;
    private int tickCounter;
    public float effectStrength;
    public LivingEntity storedTarget;

    /**
     is a list in case we want to apply multiple different attribute modifiers.
     */
    private List<UUID> attributeModifierIds = new ArrayList<>();

    public AilmentInstance(AilmentEffect effect, float sourceDamage, int duration) {
        this.effect = effect;
        this.sourceDamage = sourceDamage;
        this.duration = duration;
        this.initialDuration = duration;
    }

    public void tickEffect(LivingEntity entity, AilmentInstance instance) {
        this.effect.tick(entity, instance);
    }

    public void onExpire(LivingEntity entity, AilmentInstance instance) {
        this.effect.onExpire(entity, instance);
    }

    public boolean tickDown() {
        this.duration--;
        return this.duration <= 0;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public int getDuration() {
        return duration;
    }

    public List<UUID> getAttributeModifierIds() {
        return attributeModifierIds;
    }

    public void clearModifierIds() {
        attributeModifierIds.clear();
    }

    public void addModifierId(UUID uuid) {
        attributeModifierIds.add(uuid);
    }

    public void incrementTickCounter() {
        tickCounter++;
    }

    public void resetTickCounter() {
        tickCounter = 0;
    }

    public void refresh(int duration) {
        this.duration = duration;
    }
}
