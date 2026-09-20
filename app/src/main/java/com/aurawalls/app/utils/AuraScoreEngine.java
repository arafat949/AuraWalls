package com.aurawalls.app.utils;

import com.aurawalls.app.models.AuraPersonality;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the user's Aura Personality Type from their onboarding swipe choices.
 * Each image tag contributes points to personality types.
 */
public class AuraScoreEngine {

    // Image categories shown during onboarding
    public static final String TAG_DARK      = "dark";
    public static final String TAG_WARM      = "warm";
    public static final String TAG_NEON      = "neon";
    public static final String TAG_MINIMAL   = "minimal";
    public static final String TAG_SPACE     = "space";
    public static final String TAG_NATURE    = "nature";
    public static final String TAG_CITY      = "city";
    public static final String TAG_PASTEL    = "pastel";

    private final Map<String, Integer> scores = new HashMap<>();

    public AuraScoreEngine() {
        scores.put(TAG_DARK,    0);
        scores.put(TAG_WARM,    0);
        scores.put(TAG_NEON,    0);
        scores.put(TAG_MINIMAL, 0);
        scores.put(TAG_SPACE,   0);
        scores.put(TAG_NATURE,  0);
        scores.put(TAG_CITY,    0);
        scores.put(TAG_PASTEL,  0);
    }

    /** Call when user LIKES an image with the given tag */
    public void like(String tag) {
        Integer current = scores.get(tag);
        if (current != null) scores.put(tag, current + 2);
    }

    /** Call when user SKIPS an image (slight negative signal) */
    public void skip(String tag) {
        Integer current = scores.get(tag);
        if (current != null) scores.put(tag, Math.max(0, current - 1));
    }

    /** Compute the final personality type from accumulated scores */
    public AuraPersonality calculate() {
        String topTag = TAG_PASTEL;
        int    topScore = -1;
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            if (e.getValue() > topScore) {
                topScore = e.getValue();
                topTag   = e.getKey();
            }
        }

        AuraPersonality.Type type;
        switch (topTag) {
            case TAG_DARK:    type = AuraPersonality.Type.MIDNIGHT_DREAMER;  break;
            case TAG_WARM:    type = AuraPersonality.Type.GOLDEN_HOUR_SOUL;  break;
            case TAG_NEON:    type = AuraPersonality.Type.NEON_WANDERER;     break;
            case TAG_MINIMAL: type = AuraPersonality.Type.ZEN_MASTER;        break;
            case TAG_SPACE:   type = AuraPersonality.Type.COSMIC_EXPLORER;   break;
            case TAG_NATURE:  type = AuraPersonality.Type.WILD_HEART;        break;
            case TAG_CITY:    type = AuraPersonality.Type.URBAN_LEGEND;      break;
            default:          type = AuraPersonality.Type.PASTEL_SPIRIT;
        }
        return new AuraPersonality(type);
    }

    public Map<String, Integer> getScores() { return scores; }
}
