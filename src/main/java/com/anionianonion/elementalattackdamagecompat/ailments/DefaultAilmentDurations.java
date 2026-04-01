package com.anionianonion.elementalattackdamagecompat.ailments;

public class DefaultAilmentDurations {
    public static final int IGNITE = 80;
    public static final int BLEED = 80;
    public static final int POISON = 80;
    public static final int CHILL = 80;
    public static final int FREEZE = 40;
    public static final int SHOCK = 40;
    public static final int SCORCH = 80;
    public static final int BRITTLE = 80;
    public static final int SAP = 80;
    public static int get(Ailment ailment) {
        return switch (ailment) {
            case IGNITE -> IGNITE;
            case BLEED -> BLEED;
            case POISON -> POISON;
            case CHILL -> CHILL;
            case FREEZE -> FREEZE;
            case SHOCK -> SHOCK;
            case SCORCH -> SCORCH;
            case BRITTLE -> BRITTLE;
            case SAP -> SAP;
        };
    }
}

