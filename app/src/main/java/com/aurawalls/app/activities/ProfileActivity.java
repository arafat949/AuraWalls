package com.aurawalls.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.preference.PreferenceManager;
import com.aurawalls.app.R;
import com.aurawalls.app.utils.FavoritesManager;
import com.aurawalls.app.utils.LocaleHelper;

public class ProfileActivity extends BaseActivity {
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Aura Space");
        }
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        setupAuraCard();
        setupStats();
        setupShortcuts();
        setupSettings();
        setupInfo();
    }

    private void setupAuraCard() {
        try {
            String auraTitle = prefs.getString("aura_title", "");
            String auraMood  = prefs.getString("aura_mood", "");
            TextView tvAuraType  = findViewById(R.id.tv_aura_type);
            TextView tvAuraMood  = findViewById(R.id.tv_aura_mood);
            TextView tvAuraEmpty = findViewById(R.id.tv_aura_empty);
            if (tvAuraType != null) tvAuraType.setText(auraTitle.isEmpty() ? "Unknown Aura" : auraTitle);
            if (tvAuraMood != null) tvAuraMood.setText(auraMood.isEmpty() ? "Take the quiz" : auraMood);
            if (tvAuraEmpty != null) tvAuraEmpty.setVisibility(auraTitle.isEmpty() ? View.VISIBLE : View.GONE);
            View btnShare = findViewById(R.id.btn_share_aura);
            if (btnShare != null) btnShare.setOnClickListener(v -> shareAura(auraTitle, auraMood));
            View btnRetake = findViewById(R.id.btn_retake_quiz);
            if (btnRetake != null) btnRetake.setOnClickListener(v -> {
                prefs.edit().putBoolean("onboarding_done", false).apply();
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
            });
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "setupAuraCard error: " + e.getMessage());
        }
    }

    private void setupStats() {
        try {
            int wallpapersSet = prefs.getInt("stat_wallpapers_set", 0);
            int favCount      = FavoritesManager.getInstance(this).count();
            int healingDay    = prefs.getInt("healing_day_count", 0);
            boolean healing   = prefs.getBoolean("healing_active", false);
            TextView tvWall = findViewById(R.id.tv_stat_wallpapers);
            TextView tvFav  = findViewById(R.id.tv_stat_favorites);
            TextView tvHeal = findViewById(R.id.tv_stat_healing);
            if (tvWall != null) tvWall.setText(String.valueOf(wallpapersSet));
            if (tvFav  != null) tvFav.setText(String.valueOf(favCount));
            if (tvHeal != null) tvHeal.setText(healing ? healingDay + "/30" : "0");
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "setupStats error: " + e.getMessage());
        }
    }

    private void setupShortcuts() {
        try {
            View cardFav = findViewById(R.id.card_favorites);
            if (cardFav != null) cardFav.setOnClickListener(v ->
                startActivity(new Intent(this, FavoritesActivity.class)));
            View cardDna = findViewById(R.id.card_dna);
            if (cardDna != null) cardDna.setOnClickListener(v ->
                startActivity(new Intent(this, AuraDNAActivity.class)));
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "setupShortcuts error: " + e.getMessage());
        }
    }

    private void setupSettings() {
        try {
            TextView tvLang = findViewById(R.id.tv_current_language);
            String lang = LocaleHelper.getCurrentLanguage(this);
            if (tvLang != null) tvLang.setText("en".equals(lang) ? "English" : "Bangla");
            View rowLang = findViewById(R.id.row_language);
            if (rowLang != null) rowLang.setOnClickListener(v -> showLanguagePicker());
            int cols = prefs.getInt("grid_columns", 2);
            TextView tvGrid = findViewById(R.id.tv_current_grid);
            if (tvGrid != null) tvGrid.setText(cols + " Col");
            View rowGrid = findViewById(R.id.row_grid);
            if (rowGrid != null) rowGrid.setOnClickListener(v -> showGridPicker());
            View rowNotif = findViewById(R.id.row_notifications);
            if (rowNotif != null) rowNotif.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "setupSettings error: " + e.getMessage());
        }
    }

    private void setupInfo() {
        try {
            View rowPrivacy = findViewById(R.id.row_privacy);
            if (rowPrivacy != null) rowPrivacy.setOnClickListener(v ->
                startActivity(new Intent(this, PrivacyPolicyActivity.class)));
            View rowRate = findViewById(R.id.row_rate);
            if (rowRate != null) rowRate.setOnClickListener(v -> {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + getPackageName())));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                }
            });
            View rowShare = findViewById(R.id.row_share_app);
            if (rowShare != null) rowShare.setOnClickListener(v -> {
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("text/plain");
                share.putExtra(Intent.EXTRA_TEXT,
                    "Check out AuraWalls! https://play.google.com/store/apps/details?id=" + getPackageName());
                startActivity(Intent.createChooser(share, "Share AuraWalls"));
            });
            View rowAbout = findViewById(R.id.row_about);
            if (rowAbout != null) rowAbout.setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("AuraWalls")
                    .setMessage("Version 1.0.0 - Your vibe. Your walls.")
                    .setPositiveButton("OK", null).show());
        } catch (Exception e) {
            android.util.Log.e("ProfileActivity", "setupInfo error: " + e.getMessage());
        }
    }

    private void showLanguagePicker() {
        String[] options = {"Auto", "English", "Bangla"};
        String[] values  = {"auto", "en", "bn"};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Select Language")
            .setItems(options, (d, which) -> {
                LocaleHelper.setLanguage(this, values[which]);
                Intent intent = new Intent(this, SplashActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }).show();
    }

    private void showGridPicker() {
        String[] options = {"2 Columns", "3 Columns", "4 Columns"};
        int[] values = {2, 3, 4};
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Grid Style")
            .setItems(options, (d, which) -> {
                prefs.edit().putInt("grid_columns", values[which]).apply();
                ((TextView) findViewById(R.id.tv_current_grid)).setText(values[which] + " Col");
                Toast.makeText(this, "Grid style updated!", Toast.LENGTH_SHORT).show();
            }).show();
    }

    private void shareAura(String type, String mood) {
        String text = "My Aura: " + (type.isEmpty() ? "Unknown" : type) + " | "
            + "Mood: " + (mood.isEmpty() ? "-" : mood) + " | "
            + "Discover yours on AuraWalls! "
            + "https://play.google.com/store/apps/details?id=" + getPackageName();
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(share, "Share My Aura"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}