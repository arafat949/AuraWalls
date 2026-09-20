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
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.aurawalls.app.R;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.network.WallpaperRepository;
import com.aurawalls.app.utils.MoodHelper;
import com.aurawalls.app.utils.WallpaperSetter;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class AutoChangerWorker extends Worker {

    private static final String TAG      = "AutoChangerWorker";
    public  static final String WORK_TAG = "aura_auto_changer";
    private static final String CH_ID    = "aura_auto_ch";

    public AutoChangerWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        String mood = prefs.getString("auto_mood", "Aesthetic");
        String query = MoodHelper.getQuery(mood);

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
                    WallpaperSetter.Target target = WallpaperSetter.Target.valueOf(
                            prefs.getString("auto_target", "HOME"));
                    success.set(WallpaperSetter.setWallpaper(ctx, bmp, target));
                    if (success.get()) showNotification(ctx, pick.getAttributionText());
                } catch (Exception e) {
                    Log.e(TAG, "Glide fetch failed: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }

            @Override
            public void onError(String message) {
                Log.e(TAG, message);
                latch.countDown();
            }
        });

        try { latch.await(); } catch (InterruptedException ignored) {}
        return success.get() ? Result.success() : Result.retry();
    }

    private void showNotification(Context ctx, String attribution) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                    CH_ID, "AuraWalls Auto Changer", NotificationManager.IMPORTANCE_LOW));
        }
        NotificationCompat.Builder nb = new NotificationCompat.Builder(ctx, CH_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Wallpaper Updated")
                .setContentText(attribution)
                .setAutoCancel(true);
        nm.notify(1001, nb.build());
    }
}
