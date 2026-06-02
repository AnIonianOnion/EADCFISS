package com.anionianonion.elementalattackdamagecompat.ailments;

public enum AilmentStackingMode {
    STACKING_THEN_STRONGEST_DAMAGE, //compares sourceDamage
    STRONGEST_DURATION,
    STACKING_THEN_STRONGEST_INTENSITY, //compares effectStrength
    REFRESH_DURATION
}
