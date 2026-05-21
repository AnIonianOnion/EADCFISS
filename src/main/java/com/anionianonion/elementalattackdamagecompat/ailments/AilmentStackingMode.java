package com.anionianonion.elementalattackdamagecompat.ailments;

public enum AilmentStackingMode {
    STRONGEST_WINS, //compares sourceDamage
    ADDITIVE_STACKING,
    STRONGEST_DURATION,
    STRONGEST_INTENSITY, //compares effectStrength
    NO_STACKING_REFRESH_DURATION
}
