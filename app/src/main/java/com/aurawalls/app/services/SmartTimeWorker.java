package com.aurawalls.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aurawalls.app.R;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.network.WallpaperRepository;
import com.aurawalls.app.utils.WallpaperSetter;
import com.bumptech.glide.Glide;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * SmartTimeWorker — সময় অনুযায়ী automatically wallpaper change করে।
 * প্রতি ঘণ্টায় run হয়, দিনের time অনুযায়ী mood select করে।
 */
public class SmartTimeWorker extends Worker {

    private static final String TAG      = "SmartTimeWorker";
    public  static final String WORK_TAG = "aura_smart_time";
    private static final String CH_ID    = "aura_smart_time_ch";
    public  static final String PREF_ENABLED = "smart_time_enabled";

    public SmartTimeWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        // User যদি feature off করে থাকে
        if (!prefs.getBoolean(PREF_ENABLED, false)) return Result.success();

        String[] moodAndReason = getTimeBasedMood();
        String mood   = moodAndReason[0];
        String reason = moodAndReason[1];
        String query  = getQueryForMood(mood);

        Log.d(TAG, "Time mood: " + mood + " | " + reason);

        CountDownLatch latch   = new CountDownLatch(1);
        AtomicBoolean  success = new AtomicBoolean(false);

        WallpaperRepository.fetchWallpapers(query, 1, new WallpaperRepository.OnWallpapersLoaded() {
            @Override
            public void onEndReached() {}
            @Override
            public void onSuccess(List<WallpaperModel> wallpapers) {
                if (wallpapers.isEmpty()) { latch.countDown(); return; }
                WallpaperModel pick = wallpapers.get(new Random().nextInt(wallpapers.size()));
                try {
                    Bitmap bmp = Glide.with(ctx)
                            .asBitmap()
                            .load(pick.getFullUrl())
                            .submit()
                            .get();

                    String target = prefs.getString("auto_target", "BOTH");
                    WallpaperSetter.Target t = WallpaperSetter.Target.valueOf(target);
                    success.set(WallpaperSetter.setWallpaper(ctx, bmp, t));

                    if (success.get()) {
                        // Last set info save করো
                        prefs.edit()
                            .putString("smart_time_last_mood", mood)
                            .putString("smart_time_last_reason", reason)
                            .putLong("smart_time_last_set", System.currentTimeMillis())
                            .apply();
                        showNotification(ctx, reason, pick.getAttributionText());
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(String msg) {
                Log.e(TAG, msg);
                latch.countDown();
            }
        });

        try { latch.await(30, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
        return success.get() ? Result.success() : Result.retry();
    }

    // ── Time → Mood mapping ───────────────────────────────────────────

    public static String[] getTimeBasedMood() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 9)
            return new String[]{"Nature",    "Morning Sunrise",       "sunrise morning soft pastel"};
        if (hour >= 9 && hour < 12)
            return new String[]{"Energetic", "Morning Energy",         "bright vibrant energetic morning"};
        if (hour >= 12 && hour < 15)
            return new String[]{"Focus",     "Afternoon Focus",      "minimal clean white focus"};
        if (hour >= 15 && hour < 18)
            return new String[]{"Aesthetic", "Golden Hour",    "golden hour warm afternoon"};
        if (hour >= 18 && hour < 21)
            return new String[]{"Romantic",  "Evening Glow",    "sunset romantic warm glow"};
        return new String[]{"Dark",      "Night Silence",         "dark moody night aesthetic"};
    }

    private String getQueryForMood(String mood) {
        switch (mood) {
            case "Nature":    return "sunrise morning soft pastel landscape";
            case "Energetic": return "bright vibrant colorful energetic";
            case "Focus":     return "minimal clean white workspace";
            case "Aesthetic": return "golden hour warm afternoon aesthetic";
            case "Romantic":  return "sunset romantic warm golden";
            case "Dark":      return "dark moody night city aesthetic";
            default:          return "beautiful landscape nature";
        }
    }

    private void showNotification(Context ctx, String reason, String attribution) {
        NotificationManager nm =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                CH_ID, "Smart Time Wallpaper", NotificationManager.IMPORTANCE_LOW));
        }
        nm.notify(2001, new NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Wallpaper Updated")
            .setContentText(reason + " • " + attribution)
            .setAutoCancel(true)
            .build());
    }

    // ── Schedule / Cancel helper ──────────────────────────────────────

    public static void schedule(Context ctx) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                SmartTimeWorker.class, 1, TimeUnit.HOURS)
            .addTag(WORK_TAG)
            .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            req);
        Log.d(TAG, "SmartTimeWorker scheduled ✅");
    }

    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WORK_TAG);
        Log.d(TAG, "SmartTimeWorker cancelled");
    }
}
