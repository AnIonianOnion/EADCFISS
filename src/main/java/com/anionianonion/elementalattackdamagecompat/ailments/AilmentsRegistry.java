package com.anionianonion.elementalattackdamagecompat.ailments;

import java.util.*;

import static com.anionianonion.elementalattackdamagecompat.ModUtils.normalize;

public class AilmentsRegistry {

    private static final Map<String, Set<String>> ELEMENT_TO_AILMENTS = new HashMap<>();

    // --- ELEMENT MANAGEMENT ---

    public static void createElement(String element) {
        ELEMENT_TO_AILMENTS.putIfAbsent(normalize(element), new HashSet<>());
    }

    public static void removeElement(String element) {
        ELEMENT_TO_AILMENTS.remove(normalize(element));
    }

    // --- AILMENT MANAGEMENT ---

    public static void addDefaultAilmentToElement(String element, String ailment) {
        ELEMENT_TO_AILMENTS
                .computeIfAbsent(normalize(element), k -> new HashSet<>())
                .add(normalize(ailment));
    }

    public static void removeAilmentFromElement(String element, String ailment) {
        ELEMENT_TO_AILMENTS
                .getOrDefault(normalize(element), Set.of())
                .remove(normalize(ailment));
    }

    // --- GETTERS ---

    public static Set<String> getAilmentsForElement(String element) {
        return ELEMENT_TO_AILMENTS.getOrDefault(normalize(element), Set.of());
    }

    public static Map<String, Set<String>> getAll() {
        return Collections.unmodifiableMap(ELEMENT_TO_AILMENTS);
    }
}
