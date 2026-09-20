package com.aurawalls.app.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps human-readable mood labels to API search query strings.
 */
public class MoodHelper {

    private static final Map<String, String> MOOD_MAP = new HashMap<>();

    static {
        MOOD_MAP.put("Energetic",  "vibrant colorful dynamic sport");
        MOOD_MAP.put("Calm",       "minimalist calm peaceful pastel");
        MOOD_MAP.put("Focus",      "dark minimal workspace desk");
        MOOD_MAP.put("Romantic",   "sunset romantic soft pink floral");
        MOOD_MAP.put("Nature",     "nature forest mountain landscape");
        MOOD_MAP.put("Abstract",   "abstract art geometric pattern");
        MOOD_MAP.put("City",       "city urban skyline night lights");
        MOOD_MAP.put("Dark",       "dark moody dramatic black");
        MOOD_MAP.put("Space",      "space galaxy stars cosmos");
        MOOD_MAP.put("Aesthetic",  "aesthetic lo-fi cozy warm");
    }

    public static String getQuery(String mood) {
        return MOOD_MAP.getOrDefault(mood, mood);   // fallback: use mood as query directly
    }

    public static String[] getAllMoods() {
        return MOOD_MAP.keySet().toArray(new String[0]);
    }
}
