package com.anionianonion.elementalattackdamagecompat.pseudo_enchants;

import java.util.concurrent.atomic.AtomicInteger;

public final class GemBuffIdAllocator {
    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    /*
    AtomicIntegers can perform increments in a single step, unlike int++, which is 3 bytecode steps.

    get — returns the current value
    set — sets a new value
    incrementAndGet — increments then returns
    getAndIncrement — returns then increments
    compareAndSet — CAS operation for lock‑free algorithms
     */
    public static int nextId() {
        return NEXT_ID.getAndIncrement();
    }
}
