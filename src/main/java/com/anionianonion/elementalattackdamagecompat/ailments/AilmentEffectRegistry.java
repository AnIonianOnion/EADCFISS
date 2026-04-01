package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.*;

import java.util.EnumMap;
import java.util.Map;

public class AilmentEffectRegistry {

    private static final Map<Ailment, AilmentEffect> AILMENT_TO_AILMENT_EFFECTS =
            new EnumMap<>(Ailment.class);

    static {
        // Core PoE ailments
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.IGNITE, new IgniteEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.CHILL, new ChillEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.FREEZE, new FreezeEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.SHOCK, new ShockEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.BLEED, new BleedEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.POISON, new PoisonEffect());

        // Alternate ailments (Scorch/Brittle/Sap)
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.SCORCH, new ScorchEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.BRITTLE, new BrittleEffect());
        AILMENT_TO_AILMENT_EFFECTS.put(Ailment.SAP, new SapEffect());
    }

    public static AilmentEffect get(Ailment ailment) {
        return AILMENT_TO_AILMENT_EFFECTS.get(ailment);
    }
}
