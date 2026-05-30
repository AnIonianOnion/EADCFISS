package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.util.ModUtils;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;

public class AilmentInstance {

    ///Each AilmentInstance is corresponded 1:1 with an AilmentEffect.
    ///This class contains information about an entity's AilmentEffect, such as the number of stacks of an ailment it should have,
    /// and the strongest and total damages/strengths for those effects.

    public final AilmentEffect effect;

    public float damage;
    public float effectStrength;

    // Totals
    private float totalDamage;
    private float totalEffectStrength;

    //Strongest Stacks
    private float strongestDamage;
    private float strongestEffectStrength;

    //Duration
    private int durationInTicks;
    private int tickFrequencyCounter;
    private int frequency = 20;

    // Stack metadata (compressed)
    public final Deque<StackEntry> stacks = new ArrayDeque<>();
    public int maxStacks;

    //public LivingEntity storedTarget;

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

    public AilmentInstance(AilmentEffect effect, float dmg, float strength, int durationTicks) {
        this.effect = effect;
        //DO NOT SET VALUES OF TOTAL STRENGTH AND DAMAGE HERE, BECAUSE IT WILL BE HANDLED BY THIS CLASS'S ADD STACK
        this.damage = dmg;
        this.effectStrength = strength;
        this.durationInTicks = durationTicks;
        this.maxStacks = effect.getDefaultMaxStacks();
        if(effect instanceof DamagingAilmentEffect damagingAilmentEffect) {
            this.frequency = damagingAilmentEffect.getFrequencyInTicks();
        }
    }

    //----------Getters and Setters for EffectStrength & Damage----------

        //Values of New Stacks
        public float getEffectStrength() {
            return this.effectStrength;
        }
        public void setEffectStrength(float effectStrength) {
            this.effectStrength = effectStrength;
        }
        public float getDamage() {
            return this.damage;
        }
        public void setDamage(float damage) {
            this.damage = damage;
        }

        //Total Values of Stacks
        public float getTotalDamage() {
            return this.totalDamage;
        }
        public void setTotalDamage(float totalDamage) {
            this.totalDamage = totalDamage;
        }
        public float getTotalEffectStrength() {
            return this.totalEffectStrength;
        }
        public void setTotalEffectStrength(float totalEffectStrength) {
            this.totalEffectStrength = totalEffectStrength;
        }


        public float getStrongestDamage() {
            return this.strongestDamage;
        }
        public void setStrongestDamage(float strongestDamage) {
            this.strongestDamage = strongestDamage;
        }
        public float getStrongestEffectStrength() {
            return this.strongestEffectStrength;
        }
        public void setStrongestEffectStrength(float strongestEffectStrength) {
            this.strongestEffectStrength = strongestEffectStrength;
        }

    //Duration Functinos
    public int getDuration() {
        return this.durationInTicks;
    }
    public void refresh(int newDuration) {
        this.durationInTicks = newDuration;
    }
    public boolean tickDown() {
        this.durationInTicks--;
        return this.durationInTicks <= 0;
    }

    public void tickEffect(LivingEntity livingDefender) {
        this.effect.tick(livingDefender, this);
    }

    public void tickStacks(LivingEntity defender) {
        var iterator = this.stacks.iterator();
        while(iterator.hasNext()) {
            var stackEntry = iterator.next();
            stackEntry.durationTicks--;
            if(stackEntry.durationTicks <= 0) {

                this.effect.onStackExpire(defender, this, stackEntry, stackEntry.payload);

                this.totalDamage -= stackEntry.damage;
                this.totalEffectStrength -= stackEntry.effectStrength;

                iterator.remove();

            }
        }
        this.strongestEffectStrength = this.stacks.stream()
                .map(stack -> stack.effectStrength)
                .max(Float::compare)
                .orElse(this.strongestEffectStrength);

        this.strongestDamage = this.stacks.stream()
                .map(stack -> stack.damage)
                .max(Float::compare)
                .orElse(this.strongestDamage);
    }
    public void addStack(LivingEntity defender, LivingEntity livingAttacker, float dmg, float strength, int duration, StackEntry.ReplacementMode replacementMode, AilmentStackingMode ailmentStackingMode, Object payload) {

        //first we handle removing stacks if the amount of stacks is above the max before adding the new stack
        while(this.stacks.size() >= this.maxStacks) {
            if(ailmentStackingMode == AilmentStackingMode.NO_STACKING_REFRESH_DURATION) {
                this.stacks.getLast().durationTicks = ModUtils.getDurationInTicks(this.effect, damage, defender, livingAttacker);
            }
            else if(replacementMode == StackEntry.ReplacementMode.OLDEST) {
                StackEntry oldestStack =  this.stacks.removeFirst();
                totalDamage -= oldestStack.damage;
                totalEffectStrength -= oldestStack.effectStrength;

                //both of these fire only when the removed values were ftom the strongest stack
                if (oldestStack.damage == this.strongestDamage)
                    this.strongestDamage = this.stacks.stream().map(s -> s.damage).max(Float::compare).orElse(0f);

                if (oldestStack.effectStrength == this.strongestEffectStrength)
                    this.strongestEffectStrength = this.stacks.stream().map(s -> s.effectStrength).max(Float::compare).orElse(0f);

            }
            else {
                StackEntry weakestStack = null;
                if(ailmentStackingMode == AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY) {
                    weakestStack = this.stacks.stream()
                            .min(Comparator.comparing(stack -> stack.effectStrength))
                            .orElse(null);
                }
                else if(ailmentStackingMode == AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE) {
                    weakestStack = this.stacks.stream()
                            .min(Comparator.comparing(stack -> stack.damage))
                            .orElse(null);
                }

                if(weakestStack != null) {
                    effect.onStackExpire(defender, this, weakestStack, weakestStack.payload);

                    this.totalDamage -= weakestStack.damage;
                    this.totalEffectStrength -= weakestStack.effectStrength;

                    stacks.remove(weakestStack);
                }
            }
        }

        //now we can add it.
        if(ailmentStackingMode != AilmentStackingMode.NO_STACKING_REFRESH_DURATION) {
            this.stacks.addLast(new StackEntry(dmg, strength, duration, payload));

            this.effectStrength = strength;
            this.damage = dmg;

            this.totalDamage += dmg;
            this.totalEffectStrength += strength;

            if(dmg > this.strongestDamage) this.strongestDamage = dmg;
            if(strength > this.strongestEffectStrength) this.strongestEffectStrength = strength;
        }
    }
    public void setMaxStacks(int newMaxStacks) {
        this.maxStacks = newMaxStacks;
    }
    public int getMaxStacks() {
        return this.maxStacks;
    }
    public int getStacks() { return this.stacks.size(); }


    //2nd Order Functions
    public void onFirstApplication(LivingEntity livingDefender) {
        this.effect.onFirstApplication(livingDefender, this);
    }
    public void onLastExpiration(LivingEntity livingDefender) {
        this.effect.onLastExpiration(livingDefender, this);
    }
    public Object onStackApply(LivingEntity livingDefender) {
        return this.effect.onStackApply(livingDefender, this);
    }
    public void onStackExpire(LivingEntity livingDefender, StackEntry stackEntry, Object payload) {
        this.effect.onStackExpire(livingDefender, this, stackEntry, payload);
    }

    public void incrementTickCounter() {
        tickFrequencyCounter++;
    }
    public void resetTickCounter() {
        tickFrequencyCounter = 0;
    }
    public boolean isntTimeToDealDamage() {
        return tickFrequencyCounter < frequency;
    }
}
