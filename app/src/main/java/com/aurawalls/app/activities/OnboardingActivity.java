package com.aurawalls.app.activities;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.aurawalls.app.R;
import com.aurawalls.app.models.AuraPersonality;
import com.aurawalls.app.utils.AuraScoreEngine;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OnboardingActivity extends BaseActivity {

    private static final String TAG             = "OnboardingActivity";
    private static final String PEXELS_API_KEY  = "KQ8bNcNx7MTyhK8HK1Ox0vNv08qr2b6eDZmQ4b6EOx05SnvLhhOwRKRv";
    private static final String PREFS_SEEN_IDS  = "onboarding_seen_ids";
    private static final String PREFS_SEEN_BG   = "result_bg_seen_ids";
    private static final String PREFS_SEEN_ICON = "result_icon_seen_ids";
    private static final int    SHOW_COUNT      = 3;

    // Aggressive cache options
    private static final RequestOptions FAST_OPTS = new RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .skipMemoryCache(false)
            .encodeQuality(85);

    private static final String[][] FALLBACK_IMAGES = {
        {"https://images.pexels.com/photos/1287145/pexels-photo-1287145.jpeg", AuraScoreEngine.TAG_DARK,    "রাতের নীরবতা"},
        {"https://images.pexels.com/photos/1261728/pexels-photo-1261728.jpeg", AuraScoreEngine.TAG_WARM,    "সোনালি আলো"},
        {"https://images.pexels.com/photos/1183099/pexels-photo-1183099.jpeg", AuraScoreEngine.TAG_NEON,    "শহরের আলো"},
        {"https://images.pexels.com/photos/1212408/pexels-photo-1212408.jpeg", AuraScoreEngine.TAG_MINIMAL, "সরল সৌন্দর্য"},
        {"https://images.pexels.com/photos/1169754/pexels-photo-1169754.jpeg", AuraScoreEngine.TAG_SPACE,   "মহাকাশের ডাক"},
        {"https://images.pexels.com/photos/1366919/pexels-photo-1366919.jpeg", AuraScoreEngine.TAG_NATURE,  "প্রকৃতির কোলে"},
        {"https://images.pexels.com/photos/1486222/pexels-photo-1486222.jpeg", AuraScoreEngine.TAG_CITY,    "নগর জীবন"},
        {"https://images.pexels.com/photos/1379636/pexels-photo-1379636.jpeg", AuraScoreEngine.TAG_PASTEL,  "স্বপ্নের রঙ"},
        {"https://images.pexels.com/photos/2341830/pexels-photo-2341830.jpeg", AuraScoreEngine.TAG_DARK,    "গভীর রাত"},
        {"https://images.pexels.com/photos/3408744/pexels-photo-3408744.jpeg", AuraScoreEngine.TAG_NATURE,  "পাহাড়ের ডাক"},
        {"https://images.pexels.com/photos/1624438/pexels-photo-1624438.jpeg", AuraScoreEngine.TAG_WARM,    "সূর্যাস্তের রঙ"},
        {"https://images.pexels.com/photos/2387793/pexels-photo-2387793.jpeg", AuraScoreEngine.TAG_SPACE,   "তারার আলো"},
        {"https://images.pexels.com/photos/3651820/pexels-photo-3651820.jpeg", AuraScoreEngine.TAG_NEON,    "নিয়নের স্বপ্ন"},
        {"https://images.pexels.com/photos/1903702/pexels-photo-1903702.jpeg", AuraScoreEngine.TAG_PASTEL,  "মেঘের দেশ"},
        {"https://images.pexels.com/photos/2832034/pexels-photo-2832034.jpeg", AuraScoreEngine.TAG_MINIMAL, "শান্ত মুহূর্ত"},
        {"https://images.pexels.com/photos/2559941/pexels-photo-2559941.jpeg", AuraScoreEngine.TAG_CITY,    "শহরের রাত"},
        {"https://images.pexels.com/photos/1025469/pexels-photo-1025469.jpeg", AuraScoreEngine.TAG_DARK,    "অন্ধকারের সৌন্দর্য"},
        {"https://images.pexels.com/photos/3225517/pexels-photo-3225517.jpeg", AuraScoreEngine.TAG_NATURE,  "বনের ভেতর"},
        {"https://images.pexels.com/photos/1563356/pexels-photo-1563356.jpeg", AuraScoreEngine.TAG_WARM,    "গোলাপি আকাশ"},
        {"https://images.pexels.com/photos/1484776/pexels-photo-1484776.jpeg", AuraScoreEngine.TAG_SPACE,   "মিল্কিওয়ে"},
        {"https://images.pexels.com/photos/1323550/pexels-photo-1323550.jpeg", AuraScoreEngine.TAG_NATURE,  "ঝর্নার গান"},
        {"https://images.pexels.com/photos/2246476/pexels-photo-2246476.jpeg", AuraScoreEngine.TAG_DARK,    "রহস্যময় রাত"},
        {"https://images.pexels.com/photos/1072179/pexels-photo-1072179.jpeg", AuraScoreEngine.TAG_CITY,    "আলোর শহর"},
        {"https://images.pexels.com/photos/3617500/pexels-photo-3617500.jpeg", AuraScoreEngine.TAG_PASTEL,  "ফুলের স্বপ্ন"},
    };

    private static final String[][] FALLBACK_BG = {
        {AuraScoreEngine.TAG_DARK,    "https://images.pexels.com/photos/1252890/pexels-photo-1252890.jpeg"},
        {AuraScoreEngine.TAG_WARM,    "https://images.pexels.com/photos/1624438/pexels-photo-1624438.jpeg"},
        {AuraScoreEngine.TAG_NEON,    "https://images.pexels.com/photos/1529881/pexels-photo-1529881.jpeg"},
        {AuraScoreEngine.TAG_MINIMAL, "https://images.pexels.com/photos/2832034/pexels-photo-2832034.jpeg"},
        {AuraScoreEngine.TAG_SPACE,   "https://images.pexels.com/photos/1484776/pexels-photo-1484776.jpeg"},
        {AuraScoreEngine.TAG_NATURE,  "https://images.pexels.com/photos/1366919/pexels-photo-1366919.jpeg"},
        {AuraScoreEngine.TAG_PASTEL,  "https://images.pexels.com/photos/3617500/pexels-photo-3617500.jpeg"},
        {AuraScoreEngine.TAG_CITY,    "https://images.pexels.com/photos/2559941/pexels-photo-2559941.jpeg"},
    };

    private static final String[] SEARCH_QUERIES = {
        "dark aesthetic wallpaper", "golden hour landscape", "neon city night",
        "minimal nature", "galaxy space stars", "misty forest", "sunset ocean",
        "rainy city street", "aurora borealis", "desert dunes", "cherry blossom",
        "mountain fog", "cyberpunk street", "autumn forest", "underwater ocean"
    };

    private final List<String[]> sessionImages = new ArrayList<>();
    private int currentIndex = 0;
    private final AuraScoreEngine engine = new AuraScoreEngine();

    // Single OkHttpClient with connection pooling
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build();

    private ImageView               ivCard, ivResultBg, ivAuraIcon;
    private TextView                tvLabel, tvProgress, tvStep;
    private LinearProgressIndicator progressBar;
    private MaterialButton          btnLike, btnSkip;
    private View                    swipePanel, resultPanel;
    private MaterialCardView        cardAuraIcon;
    private TextView                tvAuraEmoji, tvAuraTitle, tvAuraDesc;
    private androidx.cardview.widget.CardView btnShare, btnContinue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_onboarding);
        bindViews();
        loadSessionImages();
    }

    private void bindViews() {
        ivCard       = findViewById(R.id.iv_onboarding_card);
        tvLabel      = findViewById(R.id.tv_card_label);
        tvProgress   = findViewById(R.id.tv_progress);
        tvStep       = findViewById(R.id.tv_step);
        progressBar  = findViewById(R.id.pb_onboarding);
        btnLike      = findViewById(R.id.btn_like);
        btnSkip      = findViewById(R.id.btn_skip);
        swipePanel   = findViewById(R.id.swipe_panel);
        resultPanel  = findViewById(R.id.result_panel);
        ivResultBg   = findViewById(R.id.iv_result_bg);
        ivAuraIcon   = findViewById(R.id.iv_aura_icon);
        cardAuraIcon = findViewById(R.id.card_aura_icon);
        tvAuraEmoji  = findViewById(R.id.tv_aura_emoji);
        tvAuraTitle  = findViewById(R.id.tv_aura_title);
        tvAuraDesc   = findViewById(R.id.tv_aura_desc);
        btnShare     = (androidx.cardview.widget.CardView) findViewById(R.id.btn_share_aura);
        btnContinue  = (androidx.cardview.widget.CardView) findViewById(R.id.btn_continue);
        btnLike.setOnClickListener(v -> onSwipe(true));
        btnSkip.setOnClickListener(v -> onSwipe(false));
    }

    // ── Session Images ────────────────────────────────────────────────

    private void loadSessionImages() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> seenIds = new HashSet<>(prefs.getStringSet(PREFS_SEEN_IDS, new HashSet<>()));

        List<String[]> unseen = new ArrayList<>();
        for (String[] img : FALLBACK_IMAGES) {
            if (!seenIds.contains(img[0])) unseen.add(img);
        }
        if (unseen.size() < SHOW_COUNT) {
            seenIds.clear();
            prefs.edit().putStringSet(PREFS_SEEN_IDS, new HashSet<>()).apply();
            Collections.addAll(unseen, FALLBACK_IMAGES);
        }
        Collections.shuffle(unseen);
        sessionImages.clear();
        Set<String> updatedSeen = new HashSet<>(seenIds);
        for (int i = 0; i < SHOW_COUNT && i < unseen.size(); i++) {
            sessionImages.add(unseen.get(i));
            updatedSeen.add(unseen.get(i)[0]);
        }
        prefs.edit().putStringSet(PREFS_SEEN_IDS, updatedSeen).apply();

        // Show first card immediately from fallback
        showCard(0);

        // Preload all fallback images now
        preloadImages(sessionImages);

        // Fetch API images in background
        fetchOnboardingImages(updatedSeen);
    }

    private void fetchOnboardingImages(Set<String> seenIds) {
        List<String> queries = new ArrayList<>();
        Collections.addAll(queries, SEARCH_QUERIES);
        Collections.shuffle(queries);
        String query = queries.get(0);

        pexelsSearch(query, 20, "medium", new PexelsCallback() {
            @Override public void onResult(List<String[]> photos) {
                List<String[]> fresh = new ArrayList<>();
                for (String[] p : photos) {
                    if (!seenIds.contains(p[1]))
                        fresh.add(new String[]{p[0], getTagForQuery(query), p[2], p[1]});
                }
                if (fresh.size() >= SHOW_COUNT) {
                    Collections.shuffle(fresh);
                    List<String[]> newSession = new ArrayList<>();
                    Set<String> updated = new HashSet<>(seenIds);
                    for (int i = 0; i < SHOW_COUNT; i++) {
                        newSession.add(fresh.get(i));
                        updated.add(fresh.get(i)[3]);
                    }
                    PreferenceManager.getDefaultSharedPreferences(OnboardingActivity.this)
                        .edit().putStringSet(PREFS_SEEN_IDS, updated).apply();

                    // Preload API images in background
                    preloadImages(newSession);

                    if (currentIndex == 0) {
                        sessionImages.clear();
                        sessionImages.addAll(newSession);
                        runOnUiThread(() -> showCard(0));
                    }
                }
            }
            @Override public void onFail() {}
        });
    }

    private void preloadImages(List<String[]> images) {
        for (String[] img : images) {
            Glide.with(getApplicationContext())
                    .load(img[0]).apply(FAST_OPTS).preload(800, 1200);
        }
    }

    // ── Card Display ──────────────────────────────────────────────────

    private void showCard(int index) {
        if (index >= sessionImages.size()) { showResult(); return; }
        String[] data = sessionImages.get(index);

        Glide.with(this).load(data[0]).apply(FAST_OPTS)
                .thumbnail(Glide.with(this).load(tinyUrl(data[0])).apply(FAST_OPTS))
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(ivCard);

        tvLabel.setText(data[2]);
        tvProgress.setText((index + 1) + " / " + sessionImages.size());
        progressBar.setMax(sessionImages.size());
        progressBar.setProgress(index + 1);

        // Preload next
        if (index + 1 < sessionImages.size()) {
            Glide.with(getApplicationContext())
                    .load(sessionImages.get(index + 1)[0])
                    .apply(FAST_OPTS).preload(800, 1200);
        }
    }

    private void onSwipe(boolean liked) {
        String tag = sessionImages.get(currentIndex)[1];
        if (liked) engine.like(tag); else engine.skip(tag);
        currentIndex++;
        showCard(currentIndex);
    }

    // ── Result ────────────────────────────────────────────────────────

    private void showResult() {
        AuraPersonality personality = engine.calculate();
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putString("aura_type",        personality.getType().name())
                .putString("aura_title",       personality.getTitle())
                .putString("aura_mood",        personality.getRecommendedMood())
                .putBoolean("onboarding_done", true).apply();

        swipePanel.setVisibility(View.GONE);
        resultPanel.setVisibility(View.VISIBLE);
        resultPanel.setAlpha(0f);
        resultPanel.animate().alpha(1f).setDuration(600).start();

        tvAuraEmoji.setText(personality.getEmoji());
        tvAuraTitle.setText(personality.getTitle());
        tvAuraDesc.setText(personality.getDescription());

        String auraType  = personality.getType().name();
        String auraColor = personality.getPrimaryColor();

        loadResultBackground(auraType);
        loadAuraIcon(auraType, auraColor);
        styleButtons(auraColor);

        cardAuraIcon.setScaleX(0f); cardAuraIcon.setScaleY(0f);
        cardAuraIcon.animate().scaleX(1f).scaleY(1f)
                .setDuration(500).setStartDelay(200)
                .setInterpolator(new OvershootInterpolator(1.5f)).start();

        btnShare.setOnClickListener(v -> {
            Intent s = new Intent(Intent.ACTION_SEND);
            s.setType("text/plain");
            s.putExtra(Intent.EXTRA_TEXT, personality.getShareText());
            startActivity(Intent.createChooser(s, "Share your Aura"));
        });
        btnContinue.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)); finish();
        });
    }

    private void loadResultBackground(String auraType) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> seen = new HashSet<>(prefs.getStringSet(PREFS_SEEN_BG, new HashSet<>()));
        String fallback = getFallbackBgUrl(auraType);

        // Instant fallback
        Glide.with(this).load(fallback).apply(FAST_OPTS)
                .thumbnail(Glide.with(this).load(tinyUrl(fallback)).apply(FAST_OPTS))
                .transition(DrawableTransitionOptions.withCrossFade(300)).into(ivResultBg);

        pexelsSearch(getAuraBgQuery(auraType), 20, "large", new PexelsCallback() {
            @Override public void onResult(List<String[]> photos) {
                List<String[]> c = new ArrayList<>();
                for (String[] p : photos) if (!seen.contains(p[1])) c.add(p);
                if (c.isEmpty()) { seen.clear(); c.addAll(photos); }
                if (c.isEmpty()) return;
                Collections.shuffle(c);
                String url = c.get(0)[0]; String id = c.get(0)[1];
                seen.add(id); prefs.edit().putStringSet(PREFS_SEEN_BG, seen).apply();
                runOnUiThread(() ->
                    Glide.with(OnboardingActivity.this).load(url).apply(FAST_OPTS)
                        .thumbnail(Glide.with(OnboardingActivity.this).load(tinyUrl(url)).apply(FAST_OPTS))
                        .transition(DrawableTransitionOptions.withCrossFade(700))
                        .placeholder(ivResultBg.getDrawable()).into(ivResultBg));
            }
            @Override public void onFail() {}
        });
    }

    private void loadAuraIcon(String auraType, String auraColor) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> seen = new HashSet<>(prefs.getStringSet(PREFS_SEEN_ICON, new HashSet<>()));
        try { cardAuraIcon.setStrokeColor(Color.parseColor(auraColor)); } catch (Exception ignored) {}
        String fallback = getFallbackBgUrl(auraType);
        Glide.with(this).load(fallback).apply(FAST_OPTS).centerCrop().into(ivAuraIcon);

        pexelsSearch(getAuraIconQuery(auraType), 15, "small", new PexelsCallback() {
            @Override public void onResult(List<String[]> photos) {
                List<String[]> c = new ArrayList<>();
                for (String[] p : photos) if (!seen.contains(p[1])) c.add(p);
                if (c.isEmpty()) { seen.clear(); c.addAll(photos); }
                if (c.isEmpty()) return;
                Collections.shuffle(c);
                String url = c.get(0)[0]; String id = c.get(0)[1];
                seen.add(id); prefs.edit().putStringSet(PREFS_SEEN_ICON, seen).apply();
                runOnUiThread(() ->
                    Glide.with(OnboardingActivity.this).load(url).apply(FAST_OPTS)
                        .transition(DrawableTransitionOptions.withCrossFade(400))
                        .centerCrop().into(ivAuraIcon));
            }
            @Override public void onFail() {}
        });
    }

    private void styleButtons(String hexColor) {
        try {
            int auraColor = Color.parseColor(hexColor);
            ValueAnimator anim = ValueAnimator.ofObject(new ArgbEvaluator(),
                    getResources().getColor(R.color.primary, null), auraColor);
            anim.setDuration(700).setStartDelay(400);
            anim.start();
            cardAuraIcon.setStrokeColor(android.content.res.ColorStateList.valueOf(auraColor));
        } catch (Exception e) { Log.e(TAG, "styleButtons: " + e.getMessage()); }
    }

    // ── Pexels ────────────────────────────────────────────────────────

    interface PexelsCallback {
        void onResult(List<String[]> photos);
        void onFail();
    }

    private void pexelsSearch(String query, int perPage, String size, PexelsCallback cb) {
        String url = "https://api.pexels.com/v1/search?query="
                + query.replace(" ", "%20") + "&per_page=" + perPage + "&orientation=portrait";
        httpClient.newCall(new Request.Builder().url(url)
                .addHeader("Authorization", PEXELS_API_KEY).build()
        ).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) { cb.onFail(); }
            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) { cb.onFail(); return; }
                try {
                    JSONArray arr = new JSONObject(response.body().string()).getJSONArray("photos");
                    List<String[]> result = new ArrayList<>();
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject p = arr.getJSONObject(i);
                        JSONObject src = p.getJSONObject("src");
                        String imgUrl = src.optString(size, src.getString("large"));
                        result.add(new String[]{imgUrl, String.valueOf(p.getInt("id")), p.getString("photographer")});
                    }
                    cb.onResult(result);
                } catch (Exception e) { cb.onFail(); }
            }
        });
    }

    // Pexels tiny thumbnail URL (40px wide — loads in milliseconds)
    private String tinyUrl(String url) {
        try {
            if (url.contains("pexels-photo-")) {
                String id = url.replaceAll(".*pexels-photo-(\\d+).*", "$1");
                if (!id.isEmpty() && id.matches("\\d+"))
                    return "https://images.pexels.com/photos/" + id
                           + "/pexels-photo-" + id + ".jpeg?auto=compress&cs=tinysrgb&w=40";
            }
        } catch (Exception ignored) {}
        return url;
    }

    private String getAuraBgQuery(String t) {
        switch (t) {
            case "MIDNIGHT_DREAMER": return "dark moody cinematic sky";
            case "GOLDEN_HOUR_SOUL": return "golden hour warm sunset";
            case "NEON_WANDERER":    return "neon lights cyberpunk city";
            case "ZEN_MASTER":       return "minimal serene white landscape";
            case "COSMIC_EXPLORER":  return "galaxy milky way cosmos stars";
            case "WILD_HEART":       return "magical misty forest nature";
            case "PASTEL_SPIRIT":    return "dreamy pastel soft flower";
            case "URBAN_LEGEND":     return "city skyline night bokeh";
            default:                 return "beautiful stunning landscape";
        }
    }

    private String getAuraIconQuery(String t) {
        switch (t) {
            case "MIDNIGHT_DREAMER": return "moon night dark stars";
            case "GOLDEN_HOUR_SOUL": return "golden sunset warm close";
            case "NEON_WANDERER":    return "neon sign colorful bokeh";
            case "ZEN_MASTER":       return "green leaf zen macro";
            case "COSMIC_EXPLORER":  return "galaxy space cosmos abstract";
            case "WILD_HEART":       return "wild forest green macro";
            case "PASTEL_SPIRIT":    return "pastel flower bloom close";
            case "URBAN_LEGEND":     return "city architecture close up";
            default:                 return "abstract colorful beautiful";
        }
    }

    private String getFallbackBgUrl(String auraType) {
        for (String[] p : FALLBACK_BG)
            if (auraType.contains(p[0]) || p[0].equals(auraType)) return p[1];
        return FALLBACK_BG[0][1];
    }

    private String getTagForQuery(String q) {
        if (q.contains("dark") || q.contains("night") || q.contains("cyber")) return AuraScoreEngine.TAG_DARK;
        if (q.contains("golden") || q.contains("sunset") || q.contains("cherry")) return AuraScoreEngine.TAG_WARM;
        if (q.contains("neon") || q.contains("city") || q.contains("street")) return AuraScoreEngine.TAG_NEON;
        if (q.contains("minimal") || q.contains("underwater")) return AuraScoreEngine.TAG_MINIMAL;
        if (q.contains("galaxy") || q.contains("space") || q.contains("aurora")) return AuraScoreEngine.TAG_SPACE;
        if (q.contains("forest") || q.contains("desert") || q.contains("ocean") || q.contains("mountain") || q.contains("autumn")) return AuraScoreEngine.TAG_NATURE;
        if (q.contains("pastel") || q.contains("rainy") || q.contains("fog")) return AuraScoreEngine.TAG_PASTEL;
        return AuraScoreEngine.TAG_NATURE;
    }
}
