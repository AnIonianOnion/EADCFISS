package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class AilmentInstance {

    public final AilmentEffect effect;

    // Totals
    public float totalDamage;
    public float totalEffectStrength;
    public float strongestEffectStrength;

    // Duration (global)
    private int durationTicks;

    private int tickFrequencyCounter;
    private int frequency = 20;

    // Stack metadata (compressed)
    public int maxStacks;
    public final Deque<StackEntry> stacks = new ArrayDeque<>();

    public LivingEntity storedTarget;

    public static class StackEntry {
        public final float damage;
        public final float effectStrength;
        public int durationTicks;
        public final Object payload; // <--- NEW

        public StackEntry(float dmg, float str, int dur, Object payload) {
            this.damage = dmg;
            this.effectStrength = str;
            this.durationTicks = dur;
            this.payload = payload;
        }

        public enum ReplacementMode {OLDEST, WEAKEST }
    }


    /**
     * unused
     is a list in case we want to apply multiple different attribute modifiers.
     */
    private List<UUID> attributeModifierIds = new ArrayList<>();

    public AilmentInstance(AilmentEffect effect, float dmg, float strength, int durationTicks) {
        this.effect = effect;
        this.totalDamage = dmg;
        this.totalEffectStrength = strength;
        this.strongestEffectStrength = strength;
        this.durationTicks = durationTicks;
        this.maxStacks = effect.getDefaultMaxStacks();
        if(effect instanceof DamagingAilmentEffect damagingAilmentEffect) {
            this.frequency = damagingAilmentEffect.getFrequencyInTicks();
        }
    }

    public int getDuration() {
        return durationTicks;
    }

    public void refresh(int newDuration) {
        this.durationTicks = newDuration;
    }

    public boolean tickDown() {
        durationTicks--;
        return durationTicks <= 0;
    }

    public void tickStacks(LivingEntity defender, AilmentEffect effect) {

        Iterator<StackEntry> it = stacks.iterator();

        while (it.hasNext()) {
            StackEntry s = it.next();
            s.durationTicks--;

            if (s.durationTicks <= 0) {

                effect.onStackExpire(defender, this, s, s.payload);

                totalDamage -= s.damage;
                totalEffectStrength -= s.effectStrength;

                it.remove();
            }
        }

        //For non-stacking nondamaging ailments, stack entries (of StackEntry) arent' added, so we need to preserve the initial value instead of resetting it.
        //but if nondamaging ailments do stack, then the strongest should be from the stack.
        strongestEffectStrength = stacks.stream()
                .map(st -> st.effectStrength)
                .max(Float::compare)
                .orElse(strongestEffectStrength);
    }

    public void addStack(LivingEntity defender, float dmg, float strength, int duration, StackEntry.ReplacementMode replacementMode, Object payload) {

        //removing the oldest stack if we are at max stacks...
        if (stacks.size() >= maxStacks) {
            if(replacementMode == StackEntry.ReplacementMode.OLDEST) {
                StackEntry oldest = stacks.removeFirst();
                totalDamage -= oldest.damage;
                totalEffectStrength -= oldest.effectStrength;    
            } else if (replacementMode == StackEntry.ReplacementMode.WEAKEST) {
                // 1. Find the weakest stack
                StackEntry weakest = stacks.stream()
                        .min(Comparator.comparing(s -> s.effectStrength))
                        .orElse(null);

                if (weakest != null) {
                    // 2. Fire onStackExpire
                    effect.onStackExpire(defender, this, weakest, weakest.payload);

                    // 3. Remove it
                    stacks.remove(weakest);

                    // 4. Update totals
                    totalDamage -= weakest.damage;
                    totalEffectStrength -= weakest.effectStrength;
                }
            }

        }

        //before we add a new one
        stacks.addLast(new StackEntry(dmg, strength, duration, payload));

        totalDamage += dmg;
        totalEffectStrength += strength;

        if (strength > strongestEffectStrength)
            strongestEffectStrength = strength;
    }

    public void setMaxStacks(int newMaxStacks) {
        this.maxStacks = newMaxStacks;
    }
    public void tickEffect(LivingEntity entity, AilmentInstance instance) {
        this.effect.tick(entity, instance);
    }

    public void onExpire(LivingEntity entity, AilmentInstance instance) {
        this.effect.onExpire(entity, instance);
    }

    public void incrementTickCounter() {
        tickFrequencyCounter++;
    }

    public boolean isntTimeToDealDamage() {
        return tickFrequencyCounter < frequency;
    }

    public void resetTickCounter() {
        tickFrequencyCounter = 0;
    }
}
