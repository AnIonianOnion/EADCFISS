package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Element;

import java.util.List;

public interface IAilmentModifiers {

    // +1 to maximum Ignite stacks, +2 to Poison stacks, etc.
    int extraStacks(Ailment ailment);

    // Replace default ailments (Ignite → Shock, Chill → Brittle, etc.)
    Ailment replace(Ailment original);

    // Override entire ailment list for an element (Fire → Scorch instead of Ignite)
    List<Ailment> alternate(Element element);

    IAilmentModifiers EMPTY = new IAilmentModifiers() {
        @Override
        public int extraStacks(Ailment ailment) { return 0; }

        @Override
        public Ailment replace(Ailment original) { return null; }

        @Override
        public List<Ailment> alternate(Element element) { return List.of(); }
    };
}
