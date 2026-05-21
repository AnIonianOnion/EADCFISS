package com.anionianonion.elementalattackdamagecompat.ailments;

import java.util.List;

public interface IAilmentModifiers {

    // +1 to maximum Ignite stacks, +2 to Poison stacks, etc.
    int getExtraStacks(String ailment);

    // Replace default ailments (Ignite → Shock, Chill → Brittle, etc.)
    String getReplacement(String originalAilment);

    // Override entire ailment list for an element (Fire → Scorch instead of Ignite)
    List<String> getAlternateAilments(String element);

    void setExtraStacks(String ailment, int stacks);

    void setReplacement(String originalAilment, String replacement);

    void setAlternateAilments(String element, List<String> ailments);

    IAilmentModifiers EMPTY = new IAilmentModifiers() {
        @Override
        public int getExtraStacks(String ailment) { return 0; }

        @Override
        public String getReplacement(String originalAilment) { return null; }

        @Override
        public List<String> getAlternateAilments(String element) { return List.of(); }

        @Override
        public void setExtraStacks(String ailment, int stacks) {

        }

        @Override
        public void setReplacement(String originalAilment, String replacement) {

        }

        @Override
        public void setAlternateAilments(String element, List<String> ailments) {

        }
    };
}
