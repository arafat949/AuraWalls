package com.aurawalls.app.models;

/**
 * Represents a user's Aura Personality Type based on wallpaper preferences.
 * Calculated from onboarding swipe choices.
 */
public class AuraPersonality {

    public enum Type {
        MIDNIGHT_DREAMER,
        GOLDEN_HOUR_SOUL,
        NEON_WANDERER,
        ZEN_MASTER,
        COSMIC_EXPLORER,
        WILD_HEART,
        URBAN_LEGEND,
        PASTEL_SPIRIT
    }

    private final Type type;
    private final String title;
    private final String description;
    private final String emoji;
    private final String primaryColor;
    private final String shareText;
    private final String recommendedMood;

    public AuraPersonality(Type type) {
        this.type = type;
        switch (type) {
            case MIDNIGHT_DREAMER:
                title          = "Midnight Dreamer";
                description    = "আপনি রাতের নীরবতায় সৌন্দর্য খোঁজেন। Dark, mysterious এবং deep — এটাই আপনার আসল রূপ।";
                emoji          = "🌙";
                primaryColor   = "#1A1A2E";
                recommendedMood= "Dark";
                shareText      = "My Aura: Midnight Dreamer 🌙 — I find beauty in darkness. #AuraWalls";
                break;
            case GOLDEN_HOUR_SOUL:
                title          = "Golden Hour Soul";
                description    = "সূর্যাস্তের উষ্ণ আলো আপনার প্রাণ। আপনি জীবনের সুন্দর মুহূর্তগুলো ধরে রাখতে ভালোবাসেন।";
                emoji          = "🌅";
                primaryColor   = "#F4A460";
                recommendedMood= "Romantic";
                shareText      = "My Aura: Golden Hour Soul 🌅 — I live for warm moments. #AuraWalls";
                break;
            case NEON_WANDERER:
                title          = "Neon Wanderer";
                description    = "আপনি শহরের জীবন্ত আলোর মধ্যে নিজেকে খোঁজেন। Bold, colorful এবং energetic।";
                emoji          = "⚡";
                primaryColor   = "#FF6584";
                recommendedMood= "Energetic";
                shareText      = "My Aura: Neon Wanderer ⚡ — I thrive in electric energy. #AuraWalls";
                break;
            case ZEN_MASTER:
                title          = "Zen Master";
                description    = "শান্তি এবং সরলতা আপনার শক্তি। Minimalist জীবনযাপনে আপনি সুখ খোঁজেন।";
                emoji          = "🍃";
                primaryColor   = "#27AE60";
                recommendedMood= "Calm";
                shareText      = "My Aura: Zen Master 🍃 — Peace is my superpower. #AuraWalls";
                break;
            case COSMIC_EXPLORER:
                title          = "Cosmic Explorer";
                description    = "মহাকাশের বিশালতা আপনাকে অনুপ্রাণিত করে। আপনি সীমাহীন সম্ভাবনায় বিশ্বাস করেন।";
                emoji          = "🚀";
                primaryColor   = "#6C63FF";
                recommendedMood= "Space";
                shareText      = "My Aura: Cosmic Explorer 🚀 — The universe is my home. #AuraWalls";
                break;
            case WILD_HEART:
                title          = "Wild Heart";
                description    = "প্রকৃতির কোলে আপনি সত্যিকারের স্বাধীনতা অনুভব করেন। Adventure আপনার রক্তে।";
                emoji          = "🌿";
                primaryColor   = "#2ECC71";
                recommendedMood= "Nature";
                shareText      = "My Aura: Wild Heart 🌿 — Nature is my true home. #AuraWalls";
                break;
            case URBAN_LEGEND:
                title          = "Urban Legend";
                description    = "শহরের ছন্দ আপনার হৃদয় স্পর্শ করে। Architecture এবং city life আপনার passion।";
                emoji          = "🏙️";
                primaryColor   = "#34495E";
                recommendedMood= "City";
                shareText      = "My Aura: Urban Legend 🏙️ — The city never sleeps, neither do I. #AuraWalls";
                break;
            default: // PASTEL_SPIRIT
                title          = "Pastel Spirit";
                description    = "নরম রঙ এবং dreamy atmosphere আপনার পছন্দ। আপনি জীবনের কোমল দিকটা দেখেন।";
                emoji          = "🌸";
                primaryColor   = "#FFB6C1";
                recommendedMood= "Aesthetic";
                shareText      = "My Aura: Pastel Spirit 🌸 — Life is soft and beautiful. #AuraWalls";
        }
    }

    public Type   getType()            { return type; }
    public String getTitle()           { return title; }
    public String getDescription()     { return description; }
    public String getEmoji()           { return emoji; }
    public String getPrimaryColor()    { return primaryColor; }
    public String getShareText()       { return shareText; }
    public String getRecommendedMood() { return recommendedMood; }
    public String getDisplayName()     { return emoji + " " + title; }
}
