package com.anionianonion.elementalattackdamagecompat.ailments;

import java.util.*;

import static com.anionianonion.elementalattackdamagecompat.ModUtils.normalize;

public class AilmentEffectRegistry {

    public enum AilmentCategory { DAMAGING, NON_DAMAGING }

    private static final Map<String, AilmentEffect> AILMENT_EFFECTS = new HashMap<>();
    private static final Map<String, AilmentCategory> AILMENT_CATEGORY = new HashMap<>();

    // --- REGISTRATION ---

    public static void registerAilment(String ailment, AilmentCategory category, AilmentEffect effect) {
        String key = normalize(ailment);

        Objects.requireNonNull(category, "Ailment category cannot be null");
        Objects.requireNonNull(effect, "Ailment effect cannot be null");

        AILMENT_EFFECTS.put(key, effect);
        AILMENT_CATEGORY.put(key, category);
    }

    public static void removeAilment(String ailment) {
        String key = normalize(ailment);
        AILMENT_EFFECTS.remove(key);
        AILMENT_CATEGORY.remove(key);
    }

    // --- GETTERS ---

    public static AilmentEffect getEffect(String ailment) {
        return AILMENT_EFFECTS.get(normalize(ailment));
    }

    public static AilmentCategory getCategory(String ailment) {
        return AILMENT_CATEGORY.get(normalize(ailment));
    }

    public static boolean isDamaging(String ailment) {
        return getCategory(ailment) == AilmentCategory.DAMAGING;
    }

    public static boolean isNonDamaging(String ailment) {
        return getCategory(ailment) == AilmentCategory.NON_DAMAGING;
    }

    public static Set<String> getAllAilments() {
        return Collections.unmodifiableSet(AILMENT_EFFECTS.keySet());
    }

    // --- CATEGORY RETRIEVAL ---

    public static Set<String> getAilmentsByCategory(AilmentCategory category) {
        Set<String> result = new HashSet<>();

        for (var entry : AILMENT_CATEGORY.entrySet()) {
            if (entry.getValue() == category) {
                result.add(entry.getKey());
            }
        }

        return Collections.unmodifiableSet(result);
    }

    public static Set<String> getDamagingAilments() {
        return getAilmentsByCategory(AilmentCategory.DAMAGING);
    }

    public static Set<String> getNonDamagingAilments() {
        return getAilmentsByCategory(AilmentCategory.NON_DAMAGING);
    }

    // --- DEBUG OUTPUT ---

    public static void printDebug() {
        System.out.println("=== AILMENT REGISTRY ===");
        for (String ailment : AILMENT_EFFECTS.keySet()) {
            System.out.println(" - " + ailment +
                    " [" + AILMENT_CATEGORY.get(ailment) + "]" +
                    " → " + AILMENT_EFFECTS.get(ailment).getClass().getSimpleName());
        }
    }
}
