package com.aurawalls.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;

import androidx.preference.PreferenceManager;

import java.util.Locale;

/**
 * LocaleHelper — handles language detection and switching.
 *
 * Priority order:
 *  1. User manually selected language (saved in prefs)
 *  2. Auto: SIM/network country = BD → Bengali
 *  3. Auto: Device locale = bn → Bengali
 *  4. Default → English
 */
public class LocaleHelper {

    public static final String PREF_LANGUAGE = "app_language";
    public static final String LANG_AUTO     = "auto";
    public static final String LANG_EN       = "en";
    public static final String LANG_BN       = "bn";

    /**
     * Call this in every Activity's attachBaseContext().
     */
    public static Context wrap(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String saved = prefs.getString(PREF_LANGUAGE, LANG_AUTO);
        String lang  = resolveLanguage(context, saved);
        return applyLocale(context, lang);
    }

    /**
     * Resolve which language to actually use.
     */
    public static String resolveLanguage(Context context, String setting) {
        if (LANG_EN.equals(setting)) return LANG_EN;
        if (LANG_BN.equals(setting)) return LANG_BN;

        // Auto mode — detect from SIM / network / device locale
        return isFromBangladesh(context) ? LANG_BN : LANG_EN;
    }

    /**
     * Returns true if the user is likely from Bangladesh.
     * Checks: SIM country, network country, device locale.
     */
    public static boolean isFromBangladesh(Context context) {
        try {
            TelephonyManager tm =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                String simCountry     = tm.getSimCountryIso();
                String networkCountry = tm.getNetworkCountryIso();
                if ("bd".equalsIgnoreCase(simCountry)
                        || "bd".equalsIgnoreCase(networkCountry)) {
                    return true;
                }
            }
        } catch (Exception ignored) {}

        // Fallback: device locale
        Locale locale = getSystemLocale(context);
        String lang   = locale.getLanguage();
        String country = locale.getCountry();
        return "bn".equalsIgnoreCase(lang) || "BD".equalsIgnoreCase(country);
    }

    /**
     * Save user's manual language preference.
     */
    public static void setLanguage(Context context, String langCode) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LANGUAGE, langCode)
                .apply();
    }

    /**
     * Get current resolved language code ("en" or "bn").
     */
    public static String getCurrentLanguage(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String saved = prefs.getString(PREF_LANGUAGE, LANG_AUTO);
        return resolveLanguage(context, saved);
    }

    public static boolean isBengali(Context context) {
        return LANG_BN.equals(getCurrentLanguage(context));
    }

    // ── Internal ──────────────────────────────────────────────────────────

    private static Context applyLocale(Context context, String langCode) {
        Locale locale = new Locale(langCode);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            return context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            context.getResources().updateConfiguration(
                    config, context.getResources().getDisplayMetrics());
            return context;
        }
    }

    private static Locale getSystemLocale(Context context) {
        Configuration config = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return config.getLocales().get(0);
        } else {
            return config.locale;
        }
    }
}
