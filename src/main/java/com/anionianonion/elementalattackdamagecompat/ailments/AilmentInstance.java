package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.AilmentEffect;
import net.minecraft.world.entity.LivingEntity;

public class AilmentInstance {
    public final AilmentEffect effect;
    public float sourceDamage;
    private int duration;
    private int tickCounter;
    public float effectStrength;
    public LivingEntity storedTarget;

    public AilmentInstance(AilmentEffect effect, float sourceDamage, int duration) {
        this.effect = effect;
        this.sourceDamage = sourceDamage;
        this.duration = duration;
    }

    public void tickEffect(LivingEntity entity, AilmentInstance instance) {
        this.effect.tick(entity, instance);
    }

    public void onExpire(LivingEntity entity, AilmentInstance instance) {
        this.effect.onExpireExtraFunction(entity, instance);
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

    public void incrementTickCounter() {
        tickCounter++;
    }

    public void resetTickCounter() {
        tickCounter = 0;
    }
}
