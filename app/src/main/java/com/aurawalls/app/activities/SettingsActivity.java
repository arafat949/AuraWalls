package com.aurawalls.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.aurawalls.app.R;
import com.aurawalls.app.services.AutoChangerWorker;
import com.aurawalls.app.services.HealingNotificationWorker;
import com.aurawalls.app.services.SmartTimeWorker;
import com.aurawalls.app.services.WeatherWallpaperWorker;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            setupAutoChanger();
            setupSmartTime();
            setupWeatherWallpaper();
            setupDownload();
            setupAppOptions();
            setupHealingMode();
            setupAbout();
            updateDynamicSummaries();
        }

        private void setupAutoChanger() {
            SwitchPreferenceCompat sw = findPreference("auto_enabled");
            ListPreference interval   = findPreference("auto_interval_hours");
            if (sw != null) {
                sw.setOnPreferenceChangeListener((pref, val) -> {
                    boolean enabled = (boolean) val;
                    if (enabled) {
                        // Smart Time off করো — conflict prevent
                        SwitchPreferenceCompat timeSw = findPreference("smart_time_enabled");
                        if (timeSw != null && timeSw.isChecked()) {
                            timeSw.setChecked(false);
                            SmartTimeWorker.cancel(requireContext());
                        }
                        scheduleAutoChanger();
                    } else {
                        cancelAutoChanger();
                    }
                    return true;
                });
            }
            if (interval != null) {
                interval.setOnPreferenceChangeListener((pref, val) -> {
                    SwitchPreferenceCompat s = findPreference("auto_enabled");
                    if (s != null && s.isChecked()) scheduleAutoChanger();
                    return true;
                });
            }
        }

        private void scheduleAutoChanger() {
            ListPreference interval = findPreference("auto_interval_hours");
            int hours = interval != null ? Integer.parseInt(interval.getValue()) : 1;
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    AutoChangerWorker.class, hours, TimeUnit.HOURS)
                    .addTag(AutoChangerWorker.WORK_TAG).build();
            WorkManager.getInstance(requireContext()).enqueueUniquePeriodicWork(
                    AutoChangerWorker.WORK_TAG, ExistingPeriodicWorkPolicy.REPLACE, req);
            Toast.makeText(getContext(),
                "✅ Auto-changer on — every " + hours + "h", Toast.LENGTH_SHORT).show();
        }

        private void cancelAutoChanger() {
            WorkManager.getInstance(requireContext())
                    .cancelAllWorkByTag(AutoChangerWorker.WORK_TAG);
            Toast.makeText(getContext(), "Auto-changer off.", Toast.LENGTH_SHORT).show();
        }

        private void setupSmartTime() {
            SwitchPreferenceCompat sw = findPreference("smart_time_enabled");
            if (sw == null) return;
            sw.setOnPreferenceChangeListener((pref, val) -> {
                boolean enabled = (boolean) val;
                if (enabled) {
                    // Auto Changer off করো — conflict prevent
                    SwitchPreferenceCompat autoSw = findPreference("auto_enabled");
                    if (autoSw != null && autoSw.isChecked()) {
                        autoSw.setChecked(false);
                        cancelAutoChanger();
                    }
                    SmartTimeWorker.schedule(requireContext());
                    Toast.makeText(getContext(),
                        "🕐 Smart Time on! Auto Changer turned off to avoid conflict.",
                        Toast.LENGTH_LONG).show();
                } else {
                    SmartTimeWorker.cancel(requireContext());
                    Toast.makeText(getContext(), "Smart Time off.", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        private void setupDownload() {
            Preference clearCache = findPreference("clear_cache");
            if (clearCache != null) {
                clearCache.setOnPreferenceClickListener(pref -> {
                    com.bumptech.glide.Glide.get(requireContext()).clearDiskCache();
                    Toast.makeText(getContext(), "✅ Cache cleared!", Toast.LENGTH_SHORT).show();
                    return true;
                });
            }
        }

        private void setupAppOptions() {
            // Rate App
            Preference rate = findPreference("rate_app");
            if (rate != null) {
                rate.setOnPreferenceClickListener(pref -> {
                    try {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("market://details?id=" + requireContext().getPackageName())));
                    } catch (Exception e) {
                        startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://play.google.com/store/apps/details?id=" + requireContext().getPackageName())));
                    }
                    return true;
                });
            }
            // Share App
            Preference share = findPreference("share_app");
            if (share != null) {
                share.setOnPreferenceClickListener(pref -> {
                    android.content.Intent i = new android.content.Intent(android.content.Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(android.content.Intent.EXTRA_TEXT,
                        "Check out AuraWalls — beautiful wallpapers for your vibe!\nhttps://play.google.com/store/apps/details?id=" + requireContext().getPackageName());
                    startActivity(android.content.Intent.createChooser(i, "Share AuraWalls"));
                    return true;
                });
            }
            // Dark Theme
            androidx.preference.SwitchPreferenceCompat darkTheme = findPreference("dark_theme");
            if (darkTheme != null) {
                darkTheme.setOnPreferenceChangeListener((pref, val) -> {
                    boolean dark = (boolean) val;
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                        dark ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                             : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    return true;
                });
            }
        }

        private void setupWeatherWallpaper() {
            SwitchPreferenceCompat sw  = findPreference("weather_wallpaper_enabled");
            EditTextPreference cityPref = findPreference("weather_city");
            if (sw != null) {
                sw.setOnPreferenceChangeListener((pref, val) -> {
                    if ((boolean) val) {
                        WeatherWallpaperWorker.schedule(requireContext());
                        Toast.makeText(getContext(),
                            "🌤️ Weather Wallpaper on!", Toast.LENGTH_LONG).show();
                    } else {
                        WeatherWallpaperWorker.cancel(requireContext());
                        Toast.makeText(getContext(), "Weather Wallpaper off.",
                            Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
            if (cityPref != null) {
                cityPref.setOnPreferenceChangeListener((pref, val) -> {
                    cityPref.setSummary((String) val);
                    SwitchPreferenceCompat weatherSw = findPreference("weather_wallpaper_enabled");
                    if (weatherSw != null && weatherSw.isChecked()) {
                        WeatherWallpaperWorker.cancel(requireContext());
                        WeatherWallpaperWorker.schedule(requireContext());
                    }
                    return true;
                });
            }
        }

        private void setupHealingMode() {
            Preference open = findPreference("open_breakup_mode");
            if (open != null) {
                open.setOnPreferenceClickListener(pref -> {
                    startActivity(new Intent(requireContext(), BreakupModeActivity.class));
                    return true;
                });
            }
        }

        private void setupAbout() {
            Preference privacy = findPreference("privacy_policy");
            if (privacy != null) {
                privacy.setOnPreferenceClickListener(pref -> {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://aurawalls.app/privacy")));
                    } catch (Exception e) {
                        Toast.makeText(getContext(),
                            "Cannot open Privacy Policy.", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                });
            }
            Preference reset = findPreference("reset_aura");
            if (reset != null) {
                reset.setOnPreferenceClickListener(pref -> {
                    new AlertDialog.Builder(requireContext())
                        .setTitle("Reset Your Aura?")
                        .setMessage("Your Aura personality will be reset and onboarding will restart.")
                        .setPositiveButton("Yes, Reset", (d, w) -> {
                            PreferenceManager.getDefaultSharedPreferences(requireContext())
                                .edit()
                                .remove("aura_type")
                                .remove("aura_title")
                                .remove("aura_mood")
                                .putBoolean("onboarding_done", false)
                                .apply();
                            Toast.makeText(getContext(),
                                "✨ Aura reset! Please restart the app.",
                                Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                    return true;
                });
            }
        }

        private void updateDynamicSummaries() {
            SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(requireContext());

            EditTextPreference cityPref = findPreference("weather_city");
            if (cityPref != null)
                cityPref.setSummary(prefs.getString("weather_city", "Dhaka"));

            Preference weatherStatus = findPreference("weather_status");
            if (weatherStatus != null) {
                String reason = prefs.getString("weather_last_reason", "");
                long lastSet  = prefs.getLong("weather_last_set", 0);
                if (!reason.isEmpty() && lastSet > 0) {
                    String time = new SimpleDateFormat("hh:mm a", Locale.getDefault())
                        .format(new Date(lastSet));
                    weatherStatus.setSummary(reason + " — " + time);
                }
            }

            Preference healingStatus = findPreference("healing_status");
            if (healingStatus != null) {
                boolean active = prefs.getBoolean(HealingNotificationWorker.PREF_ACTIVE, false);
                if (active) {
                    int day = prefs.getInt(HealingNotificationWorker.PREF_DAY_COUNT, 0);
                    healingStatus.setSummary("💙 Active — Day " + day + "/30");
                }
            }

            Preference smartInfo = findPreference("smart_time_info");
            if (smartInfo != null) {
                String reason = prefs.getString("smart_time_last_reason", "");
                if (!reason.isEmpty())
                    smartInfo.setSummary("Last: " + reason);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() { finish(); return true; }
}
