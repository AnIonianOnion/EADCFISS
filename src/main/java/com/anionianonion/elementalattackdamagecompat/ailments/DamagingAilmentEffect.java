package com.anionianonion.elementalattackdamagecompat.ailments;

public abstract class DamagingAilmentEffect implements AilmentEffect {

    private final int frequencyInTicks;

    public DamagingAilmentEffect() {
        this.frequencyInTicks = frequencyInTicks();
    }

    protected abstract int frequencyInTicks();

    public int getFrequencyInTicks() {
        return frequencyInTicks;
    }
}
