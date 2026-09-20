package com.aurawalls.app.activities;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurawalls.app.R;
import com.aurawalls.app.adapters.WallpaperAdapter;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.network.WallpaperRepository;
import com.aurawalls.app.services.HealingNotificationWorker;
import com.aurawalls.app.utils.LocaleHelper;
import com.aurawalls.app.utils.WallpaperSetter;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BreakupModeActivity extends BaseActivity {

    private static final String TAG = "BreakupModeActivity";

    private static final String[] HEALING_QUERIES = {
        "soft pastel nature peaceful", "gentle sunrise morning hope",
        "calm ocean waves peaceful",   "flowers blooming spring hope",
        "cozy indoor soft warm light", "misty forest peaceful serene",
        "candle light warm cozy",      "cherry blossom soft pink",
        "moon night peaceful stars",   "butterfly flower garden soft"
    };

    private static final String[][] MESSAGES_EN = {
        {"Today is yours 💙",         "Feeling some pain? That's okay. Pain means you truly loved."},
        {"You are not alone 🌟",      "Millions have walked this path. You will too."},
        {"Breathe, keep going 🌸",    "Today, do one small thing that makes you happy."},
        {"See your strength 💪",      "You're still here. Every day you get through is a victory."},
        {"New morning 🌅",            "5 days in. You're doing great."},
        {"You're changing ✨",        "Pain makes people deeper. You're becoming more beautiful."},
        {"One week! 🎉",              "You've made it through a whole week!"},
        {"Your story isn't over 📖",  "This is just the end of one chapter."},
        {"Give yourself time ⏰",     "There are no shortcuts to healing."},
        {"10 days! Amazing 🌟",       "Ten days down. You're getting stronger."},
        {"Miss your smile 😊",        "Try to smile once today."},
        {"Start something new 🎨",    "Pick up a new hobby."},
        {"You are enough 💎",         "You are enough, just as you are."},
        {"Two weeks! 🏆",             "Incredible! 14 days!"},
        {"The future is bright 🌈",   "Your rainbow is coming."},
        {"You're growing 🌱",         "This pain is making you bigger."},
        {"Look at yourself 🪞",       "You're okay. You're strong."},
        {"You inspire 💫",            "You're a real hero."},
        {"19 days — nearly there! 🎯","Just 11 more days."},
        {"You are worthy ❤️",         "You deserve everything good."},
        {"3 weeks! Incredible 🌟",    "21 days completed!"},
        {"Dream new dreams 🌙",       "Beautiful new dreams are waiting for you."},
        {"You deserve love 💖",       "The right person will come one day."},
        {"Can you see the light? 🕯️", "You're close to that light."},
        {"25 days — you're a winner 🏅","You are your own hero."},
        {"Your journey inspires 🌟",  "Your story will one day give others strength."},
        {"Almost there! 💫",          "Just 3 more days."},
        {"You've been reborn 🦋",     "You are a butterfly now."},
        {"Tomorrow is victory day! 🎊","Tomorrow marks 30 days."},
        {"🎊 You did it! 30 days!",   "You've proved it — you're unstoppable! ❤️"},
    };

    private static final String[][] MESSAGES_BN = {
        {"আজকের দিনটা তোমার 💙",    "একটু কষ্ট হচ্ছে, তাই না? এটা ঠিক আছে।"},
        {"তুমি একা নও 🌟",           "লক্ষ লক্ষ মানুষ এই পথ পার করেছে। তুমিও পারবে।"},
        {"শ্বাস নাও, এগিয়ে যাও 🌸", "আজকে একটা ছোট কাজ করো যেটা তোমাকে খুশি করে।"},
        {"তোমার শক্তি দেখো 💪",     "তুমি এখনো এখানে আছো। প্রতিটা দিন পার করা জয়।"},
        {"নতুন সকাল 🌅",             "৫ দিন হয়ে গেছে। তুমি ভালো করছো।"},
        {"তুমি বদলাচ্ছো ✨",         "কষ্ট মানুষকে গভীর করে। তুমি আরো সুন্দর হচ্ছো।"},
        {"এক সপ্তাহ! 🎉",            "তুমি একটা পুরো সপ্তাহ পার করেছো!"},
        {"তোমার গল্প শেষ হয়নি 📖",  "এটা শুধু একটা অধ্যায়ের শেষ।"},
        {"নিজেকে সময় দাও ⏰",       "Healing এর কোনো shortcuts নেই।"},
        {"১০ দিন! তুমি দারুণ 🌟",    "দশটা দিন পার করেছো। তুমি শক্তিশালী হচ্ছো।"},
        {"তোমার হাসি মিস করি 😊",   "আজকে একবার হাসার চেষ্টা করো।"},
        {"নতুন কিছু শুরু করো 🎨",   "একটা নতুন hobby শুরু করো।"},
        {"তুমি যথেষ্ট 💎",           "তুমি নিজেই যথেষ্ট সুন্দর।"},
        {"দুই সপ্তাহ! 🏆",           "অবিশ্বাস্য! ১৪ দিন!"},
        {"ভবিষ্যৎ উজ্জ্বল 🌈",       "তোমার রংধনু আসছে।"},
        {"তুমি grow করছো 🌱",        "এই কষ্টটাই তোমাকে বড় করছে।"},
        {"নিজেকে দেখো 🪞",           "তুমি ভালো আছো, তুমি strong।"},
        {"তুমি inspire করো 💫",      "তুমি সত্যিকারের hero।"},
        {"১৯ দিন — প্রায় শেষে! 🎯", "আর মাত্র ১১ দিন।"},
        {"তুমি worthy ❤️",            "তুমি সব পাওয়ার যোগ্য।"},
        {"৩ সপ্তাহ! অসাধারণ 🌟",    "২১ দিন পার করেছো!"},
        {"নতুন স্বপ্ন দেখো 🌙",      "সুন্দর নতুন স্বপ্ন তোমার জন্য অপেক্ষা করছে।"},
        {"তুমি ভালোবাসার যোগ্য 💖",  "সঠিক মানুষ একদিন আসবে।"},
        {"আলো দেখতে পাচ্ছো? 🕯️",   "তুমি সেই আলোর কাছাকাছি।"},
        {"২৫ দিন — তুমি winner 🏅",  "তুমিই তোমার hero।"},
        {"তোমার journey অনুপ্রেরণা 🌟","তোমার গল্প একদিন অন্যকে শক্তি দেবে।"},
        {"প্রায় শেষ! 💫",            "মাত্র ৩ দিন বাকি।"},
        {"তুমি নতুন হয়ে উঠেছো 🦋",  "তুমি এখন butterfly।"},
        {"কাল তোমার বিজয় দিন! 🎊",  "আগামীকাল ৩০ দিন পূর্ণ হবে।"},
        {"🎊 তুমি করেছো! 30 দিন!",   "তুমি প্রমাণ করেছো — তুমি অপ্রতিরোধ্য! ❤️"},
    };

    private static final String[] QUOTES_EN = {
        "\"Pain doesn't break you — it builds you.\"",
        "\"After every storm, the sun comes out.\"",
        "\"You are so much stronger than you think.\"",
        "\"Losing love doesn't mean losing yourself.\"",
        "\"Your story doesn't end here — the best is coming.\"",
        "\"You are enough. You always were enough.\"",
    };

    private static final String[] QUOTES_BN = {
        "\"কষ্ট তোমাকে ভাঙে না, তোমাকে গড়ে।\"",
        "\"প্রতিটা ঝড়ের পরে রোদ আসে।\"",
        "\"তুমি যতটা ভাবো তার চেয়ে অনেক বেশি strong।\"",
        "\"ভালোবাসা হারানো মানে নিজেকে হারানো না।\"",
        "\"তোমার গল্প এখানেই শেষ না — সেরাটা আসছে।\"",
        "\"তুমি যথেষ্ট। তুমি সবসময় যথেষ্ট ছিলে।\"",
    };

    private View               layoutActive, layoutInactive;
    private TextView           tvDayCount, tvTodayMessage, tvQuote;
    private LinearProgressIndicator pbHealing;
    private MaterialButton     btnStartHealing, btnStopHealing;
    private RecyclerView       rvHealingWallpapers;
    private WallpaperAdapter   adapter;
    private List<WallpaperModel> wallpapers = new ArrayList<>();
    private SharedPreferences  prefs;
    private boolean            isBengali;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakup_mode);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.healing_mode_title));
        }
        prefs     = PreferenceManager.getDefaultSharedPreferences(this);
        isBengali = LocaleHelper.isBengali(this);
        bindViews();
        setupRecyclerView();
        updateUI();
        loadHealingWallpapers();
        setRandomQuote();
        if (prefs.getBoolean(HealingNotificationWorker.PREF_ACTIVE, false)) autoSetHealingWallpaper();
    }

    private void bindViews() {
        layoutActive        = findViewById(R.id.layout_active);
        layoutInactive      = findViewById(R.id.layout_inactive);
        tvDayCount          = findViewById(R.id.tv_day_count);
        tvTodayMessage      = findViewById(R.id.tv_today_message);
        pbHealing           = findViewById(R.id.pb_healing);
        btnStartHealing     = findViewById(R.id.btn_start_healing);
        btnStopHealing      = findViewById(R.id.btn_stop_healing);
        rvHealingWallpapers = findViewById(R.id.rv_healing_wallpapers);
        tvQuote             = findViewById(R.id.tv_quote);
        btnStartHealing.setOnClickListener(v -> startHealingMode());
        btnStopHealing.setOnClickListener(v  -> confirmStop());
    }

    private void setupRecyclerView() {
        adapter = new WallpaperAdapter(this, wallpapers);
        rvHealingWallpapers.setLayoutManager(new GridLayoutManager(this, 2));
        rvHealingWallpapers.setAdapter(adapter);
        rvHealingWallpapers.setNestedScrollingEnabled(false);
    }

    private void updateUI() {
        boolean isActive = prefs.getBoolean(HealingNotificationWorker.PREF_ACTIVE, false);
        if (isActive) {
            layoutActive.setVisibility(View.VISIBLE);
            layoutInactive.setVisibility(View.GONE);
            int day = prefs.getInt(HealingNotificationWorker.PREF_DAY_COUNT, 1);
            tvDayCount.setText("Day " + day + "/30");
            pbHealing.setMax(30);
            pbHealing.setProgress(day);
            int msgIdx = Math.max(0, Math.min(day - 1, 29));
            String[][] msgs = isBengali ? MESSAGES_BN : MESSAGES_EN;
            tvTodayMessage.setText(msgs[msgIdx][0] + "\n\n" + msgs[msgIdx][1]);
        } else {
            layoutActive.setVisibility(View.GONE);
            layoutInactive.setVisibility(View.VISIBLE);
        }
    }

    private void startHealingMode() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.healing_start_title))
            .setMessage(getString(R.string.healing_start_msg))
            .setPositiveButton(getString(R.string.healing_start_yes), (d, w) -> {
                HealingNotificationWorker.schedule(this);
                autoSetHealingWallpaper();
                updateUI();
                Toast.makeText(this, getString(R.string.healing_started_toast), Toast.LENGTH_LONG).show();
            })
            .setNegativeButton(getString(R.string.healing_start_later), null)
            .show();
    }

    private void confirmStop() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.healing_stop_title))
            .setMessage(getString(R.string.healing_stop_msg))
            .setPositiveButton(getString(R.string.healing_stop_yes), (d, w) -> {
                HealingNotificationWorker.cancel(this);
                updateUI();
                Toast.makeText(this, getString(R.string.healing_stopped_toast), Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton(getString(R.string.healing_stop_no), null)
            .show();
    }

    private void loadHealingWallpapers() {
        String query = HEALING_QUERIES[new Random().nextInt(HEALING_QUERIES.length)];
        WallpaperRepository.fetchWallpapers(query, 1, new WallpaperRepository.OnWallpapersLoaded() {
            @Override public void onEndReached() {}
            @Override public void onSuccess(List<WallpaperModel> list) {
                runOnUiThread(() -> { wallpapers.clear(); wallpapers.addAll(list); adapter.notifyDataSetChanged(); });
            }
            @Override public void onError(String msg) { Log.e(TAG, msg); }
        });
    }

    private void autoSetHealingWallpaper() {
        String query = HEALING_QUERIES[new Random().nextInt(HEALING_QUERIES.length)];
        Executors.newSingleThreadExecutor().execute(() ->
            WallpaperRepository.fetchWallpapers(query, 1, new WallpaperRepository.OnWallpapersLoaded() {
                @Override public void onSuccess(List<WallpaperModel> list) {
                    if (list.isEmpty()) return;
                    try {
                        Bitmap bmp = Glide.with(getApplicationContext())
                                .asBitmap().load(list.get(0).getFullUrl()).submit().get();
                        WallpaperSetter.setWallpaper(getApplicationContext(), bmp, WallpaperSetter.Target.BOTH);
                    } catch (Exception e) { Log.e(TAG, "Auto set failed: " + e.getMessage()); }
                }
                @Override public void onEndReached() {}
                @Override public void onError(String msg) {}
            })
        );
    }

    private void setRandomQuote() {
        if (tvQuote == null) return;
        String[] quotes = isBengali ? QUOTES_BN : QUOTES_EN;
        tvQuote.setText(quotes[new Random().nextInt(quotes.length)]);
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
