package com.aurawalls.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.aurawalls.app.R;
import com.aurawalls.app.adapters.WallpaperAdapter;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.network.WallpaperRepository;
import com.aurawalls.app.services.AutoChangerWorker;
import com.aurawalls.app.services.HealingNotificationWorker;
import com.aurawalls.app.services.SmartTimeWorker;
import com.aurawalls.app.utils.MoodHelper;
import com.aurawalls.app.utils.SeasonalPackHelper;
import com.aurawalls.app.utils.WeatherThemeHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity {

    private WallpaperAdapter        adapter;
    private List<WallpaperModel>    wallpapers  = new ArrayList<>();
    private LinearProgressIndicator progressBar;
    private String  currentQuery    = "aesthetic";
    private int     currentPage     = 1;
    private boolean isLoading       = false;
    private MaterialCardView auraBanner;
    private TextView tvAuraGreeting, tvWeatherTag;

    // Real-time search debounce
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean("onboarding_done", false)) {
            startActivity(new Intent(this, OnboardingActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Mood query from MoodCameraActivity
        String moodQuery = getIntent().getStringExtra("mood_query");
        if (moodQuery != null && !moodQuery.isEmpty()) currentQuery = moodQuery;

        initAdMob();
        setupToolbar();
        setupAuraBanner(prefs);
        setupSeasonalBanner();
        setupWeatherTheme(prefs);
        setupHealingBanner(prefs);
        setupMoodChips(prefs);
        setupRecyclerView();
        setupBottomNav();
        startWorkers(prefs);
        loadWallpapers(true);
    }

    // ── Bottom Navigation ─────────────────────────────────────────────

    private void setupBottomNav() {
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav == null) return;
        nav.setSelectedItemId(R.id.nav_home);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                RecyclerView rv = findViewById(R.id.rv_wallpapers);
                if (rv != null) rv.smoothScrollToPosition(0);
                return true;
            }
            if (id == R.id.nav_video) {
                startActivity(new Intent(this, VideoActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_dna) {
                startActivity(new Intent(this, AuraDNAActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_mood) {
                startActivity(new Intent(this, MoodCameraActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    // ── AdMob ─────────────────────────────────────────────────────────

    private void initAdMob() {
        MobileAds.initialize(this, s -> {});
        AdView av = findViewById(R.id.ad_banner);
        if (av != null) av.loadAd(new AdRequest.Builder().build());
    }

    // ── Toolbar ───────────────────────────────────────────────────────

    private void setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar));
    }

    // ── Aura Banner ───────────────────────────────────────────────────

    private void setupAuraBanner(SharedPreferences prefs) {
        auraBanner     = findViewById(R.id.aura_banner);
        tvAuraGreeting = findViewById(R.id.tv_aura_greeting);
        tvWeatherTag   = findViewById(R.id.tv_weather_tag);

        String auraTitle = prefs.getString("aura_title", "");
        if (!auraTitle.isEmpty() && auraBanner != null) {
            auraBanner.setVisibility(View.VISIBLE);
            tvAuraGreeting.setText("✨ Your Aura: " + auraTitle);
        }

        if (auraBanner != null) {
            auraBanner.setOnClickListener(v -> {
                prefs.edit().putBoolean("onboarding_done", false).apply();
                startActivity(new Intent(this, OnboardingActivity.class));
                finish();
            });
        }
    }

    // ── Seasonal Banner ───────────────────────────────────────────────

    private void setupSeasonalBanner() {
        MaterialCardView sb = findViewById(R.id.seasonal_banner);
        TextView ts = findViewById(R.id.tv_seasonal_name);
        TextView tm = findViewById(R.id.tv_seasonal_msg);
        if (sb == null) return;

        SeasonalPackHelper.SeasonalTheme theme = SeasonalPackHelper.getActiveTheme();
        if (theme != null) {
            sb.setVisibility(View.VISIBLE);
            ts.setText(theme.emoji + " " + theme.name);
            tm.setText(theme.message);
            sb.setOnClickListener(v -> { currentQuery = theme.query; loadWallpapers(true); });
        } else {
            sb.setVisibility(View.GONE);
        }
    }

    // ── Weather Theme ─────────────────────────────────────────────────

    private void setupWeatherTheme(SharedPreferences prefs) {
        // City from settings — no longer hardcoded
        String city = prefs.getString("weather_city", "Dhaka");
        WeatherThemeHelper.getRecommendedMood(this, city, (mood, reason) ->
            runOnUiThread(() -> {
                if (tvWeatherTag != null) {
                    tvWeatherTag.setText(reason);
                    tvWeatherTag.setVisibility(View.VISIBLE);
                }
                if (prefs.getBoolean("auto_weather_mood", true))
                    currentQuery = MoodHelper.getQuery(mood);
            })
        );
    }

    // ── Healing Banner ────────────────────────────────────────────────

    private void setupHealingBanner(SharedPreferences prefs) {
        MaterialCardView healingBanner = findViewById(R.id.healing_banner);
        if (healingBanner == null) return;

        boolean active = prefs.getBoolean(HealingNotificationWorker.PREF_ACTIVE, false);
        TextView tv = findViewById(R.id.tv_healing_text);

        healingBanner.setVisibility(View.VISIBLE);
        if (active) {
            int day = prefs.getInt(HealingNotificationWorker.PREF_DAY_COUNT, 0);
            if (tv != null) tv.setText("💙 Healing Journey — Day " + day + "/30");
        } else {
            if (tv != null) tv.setText("💙 Feeling low? Start your Healing Journey →");
        }
        healingBanner.setOnClickListener(v ->
            startActivity(new Intent(this, BreakupModeActivity.class)));
    }

    // ── Mood Chips ────────────────────────────────────────────────────

    private void setupMoodChips(SharedPreferences prefs) {
        ChipGroup cg = findViewById(R.id.chip_group_moods);
        if (cg == null) return;

        String auraMood = prefs.getString("aura_mood", "");

        // My Aura chip first
        if (!auraMood.isEmpty()) {
            Chip ac = new Chip(this);
            ac.setText("⭐ My Aura");
            ac.setCheckable(true);
            ac.setChecked(true);
            ac.setOnCheckedChangeListener((v, c) -> {
                if (c) { currentQuery = MoodHelper.getQuery(auraMood); loadWallpapers(true); }
            });
            cg.addView(ac);
        }

        // All mood chips
        for (String mood : MoodHelper.getAllMoods()) {
            Chip chip = new Chip(this);
            chip.setText(mood);
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((v, c) -> {
                if (c) { currentQuery = MoodHelper.getQuery(mood); loadWallpapers(true); }
            });
            cg.addView(chip);
        }

        if (auraMood.isEmpty() && cg.getChildCount() > 0)
            ((Chip) cg.getChildAt(0)).setChecked(true);
    }

    // ── RecyclerView ──────────────────────────────────────────────────

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_wallpapers);
        adapter = new WallpaperAdapter(this, wallpapers);

        // Dynamic grid columns from settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int columns = 2; // Fixed 2-column grid

        GridLayoutManager glm = new GridLayoutManager(this, columns);
        glm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override public int getSpanSize(int p) {
                return adapter.getItemViewType(p) == 1 ? columns : 1;
            }
        });

        rv.setLayoutManager(glm);
        rv.setAdapter(adapter);
        rv.setHasFixedSize(false);

        // Infinite scroll
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(@androidx.annotation.NonNull RecyclerView r, int dx, int dy) {
                GridLayoutManager l = (GridLayoutManager) r.getLayoutManager();
                if (!isLoading && l != null &&
                        l.findLastVisibleItemPosition() >= adapter.getItemCount() - 6) {
                    currentPage++;
                    loadWallpapers(false);
                }
            }
        });

        progressBar = findViewById(R.id.progress_bar);
    }

    // ── Workers ───────────────────────────────────────────────────────

    private void startWorkers(SharedPreferences prefs) {
        if (prefs.getBoolean(SmartTimeWorker.PREF_ENABLED, false)) {
            SmartTimeWorker.schedule(this);
        }
        if (prefs.getBoolean("auto_enabled", false)) {
            String intervalStr = prefs.getString("auto_interval_hours", "1");
            int hours = Integer.parseInt(intervalStr);
            PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                    AutoChangerWorker.class, Math.max(1, hours), TimeUnit.HOURS)
                .addTag(AutoChangerWorker.WORK_TAG)
                .build();
            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                AutoChangerWorker.WORK_TAG, ExistingPeriodicWorkPolicy.UPDATE, req);
        }
    }

    // ── Load Wallpapers ───────────────────────────────────────────────

    private void loadWallpapers(boolean reset) {
        if (isLoading) return;

        // No internet check
        if (!isNetworkAvailable()) {
            View noInternet = findViewById(R.id.no_internet_view);
            if (noInternet != null) {
                noInternet.setVisibility(View.VISIBLE);
                View retryBtn = findViewById(R.id.btn_retry);
                if (retryBtn != null) retryBtn.setOnClickListener(v -> {
                    noInternet.setVisibility(View.GONE);
                    loadWallpapers(true);
                });
            }
            return;
        }

        View noInternet = findViewById(R.id.no_internet_view);
        if (noInternet != null) noInternet.setVisibility(View.GONE);

        isLoading = true;
        if (reset) {
            currentPage = 1;
            wallpapers.clear();
            adapter.notifyDataSetChanged();
        }
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        WallpaperRepository.fetchWallpapers(currentQuery, currentPage,
            new WallpaperRepository.OnWallpapersLoaded() {
                @Override public void onEndReached() {
                    runOnUiThread(() -> {
                        isLoading = false;
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                    });
                }
                @Override public void onSuccess(List<WallpaperModel> items) {
                    runOnUiThread(() -> {
                        adapter.addItems(items);
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        isLoading = false;

                        // Empty state
                        View emptyView = findViewById(R.id.empty_view);
                        if (emptyView != null)
                            emptyView.setVisibility(wallpapers.isEmpty() ? View.VISIBLE : View.GONE);
                    });
                }
                @Override public void onError(String msg) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
                        if (progressBar != null) progressBar.setVisibility(View.GONE);
                        isLoading = false;
                    });
                }
            });
    }

    // ── Menu ──────────────────────────────────────────────────────────

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) searchItem.getActionView();

        sv.setQueryHint("Search wallpapers...");

        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String q) {
                currentQuery = q.trim();
                loadWallpapers(true);
                sv.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // Real-time search with 600ms debounce
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                if (s.trim().length() >= 3) {
                    searchRunnable = () -> {
                        currentQuery = s.trim();
                        loadWallpapers(true);
                    };
                    searchHandler.postDelayed(searchRunnable, 600);
                }
                return false;
            }
        });

        // Search closed → restore Aura mood
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override public boolean onMenuItemActionExpand(MenuItem item) { return true; }
            @Override public boolean onMenuItemActionCollapse(MenuItem item) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                String auraMood = prefs.getString("aura_mood", "Aesthetic");
                currentQuery = MoodHelper.getQuery(auraMood);
                loadWallpapers(true);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_favorites) {
            startActivity(new Intent(this, FavoritesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ── Lifecycle ─────────────────────────────────────────────────────

    @Override
    protected void onResume() {
        super.onResume();
        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        if (nav != null) nav.setSelectedItemId(R.id.nav_home);
        // Refresh favorites state
        adapter.notifyDataSetChanged();
    }

    private long backPressedTime = 0;

    @Override
    public void onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backPressedTime = System.currentTimeMillis();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager cm =
            (android.net.ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        android.net.NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }
}
