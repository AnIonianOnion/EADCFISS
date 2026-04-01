package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Element;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class DefaultAilments {

    private static final Map<Element, List<Ailment>> DEFAULT_AILMENTS =
            new EnumMap<>(Element.class);

    static {
        DEFAULT_AILMENTS.put(Element.FIRE, List.of(Ailment.IGNITE));
        DEFAULT_AILMENTS.put(Element.ICE, List.of(Ailment.CHILL, Ailment.FREEZE));
        DEFAULT_AILMENTS.put(Element.LIGHTNING, List.of(Ailment.SHOCK));
        DEFAULT_AILMENTS.put(Element.BLOOD, List.of(Ailment.BLEED));
        DEFAULT_AILMENTS.put(Element.ABYSSAL, List.of(Ailment.POISON));
        // etc.
    }

    public static List<Ailment> get(Element element) {
        return DEFAULT_AILMENTS.getOrDefault(element, List.of());
    }
}
