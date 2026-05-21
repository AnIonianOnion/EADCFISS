package com.anionianonion.elementalattackdamagecompat.util;

import java.util.Locale;

public class ModUtils {

    public static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT).trim();
    }

    public static String capitalize(String word) {
        String firstLetter = word.substring(0, 1).toUpperCase();
        String restOfString = word.substring(1);

        return firstLetter + restOfString;
    }
}
