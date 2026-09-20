package com.aurawalls.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.aurawalls.app.activities.BreakupModeActivity;

import java.util.concurrent.TimeUnit;

public class HealingNotificationWorker extends Worker {

    private static final String TAG    = "HealingWorker";
    public static final String WORK_TAG     = "aura_healing_journey";
    private static final String CH_ID       = "aura_healing_ch";
    public static final String PREF_START_DAY = "breakup_start_day";
    public static final String PREF_ACTIVE    = "breakup_mode_active";
    public static final String PREF_DAY_COUNT = "healing_day_count";

    private static final String[][] HEALING_MESSAGES = {
        {"Today is yours",              "Feeling some pain? That is okay. It means you truly loved."},
        {"You are not alone",           "Millions have walked this path. You will too. One day this will just be a memory."},
        {"Breathe, keep going",         "Do one small thing today that makes you happy. You deserve it."},
        {"See your strength",           "You are still here. Every day you get through is a victory."},
        {"New morning",                 "5 days in. You are doing great. Show yourself some love today."},
        {"You are changing",            "Pain makes people deeper. You are becoming a more beautiful person."},
        {"One week!",                   "You made it through a whole week! That is not small — that is a big win."},
        {"Your story is not over",      "This is just the end of one chapter. Your most beautiful chapter is still coming."},
        {"Give yourself time",          "There are no shortcuts to healing. Take all the time you need."},
        {"10 days! Amazing",            "Ten days down. Every single day you are getting stronger."},
        {"We miss your smile",          "Try to smile today — even for a small reason. Your smile makes the world better."},
        {"Start something new",         "A new hobby, a new place, a new conversation. Life is waiting for you."},
        {"You are enough",              "You do not need to change for anyone. You are already beautiful enough."},
        {"Two weeks!",                  "Incredible! 14 days! You have proven how strong you really are."},
        {"The future is bright",        "After every storm comes a rainbow. Yours is on the way. Hold on."},
        {"You are growing",             "This pain is making you bigger. That is life — and it is beautiful."},
        {"Look at yourself",            "Stand in front of a mirror and say: I am okay. I am strong. Believe it."},
        {"You inspire others",          "People who keep going through pain — they are the real heroes."},
        {"19 days — almost there!",     "Just 11 more days. You have come so far. Do not stop now."},
        {"You are worthy",              "Good people, good relationships, good life — you deserve all of it."},
        {"3 weeks! Incredible",         "21 days! Psychology says habits form in 21 days. You have made healing a habit!"},
        {"Dream new dreams",            "Let go of the old dreams. New, more beautiful ones are waiting for you."},
        {"You deserve love",            "The right person will come. And they will love you exactly as you deserve."},
        {"Can you see the light?",      "There is always light at the end of the dark. You are almost there."},
        {"25 days — you are a winner!", "Look how far you have come. You are your own hero."},
        {"Your journey inspires",       "Your story of pain and strength will one day give someone else hope."},
        {"Almost there!",               "Just 3 more days. You have really done this."},
        {"You have been reborn",        "Before a butterfly, there is a cocoon. You are the butterfly now."},
        {"Tomorrow is your victory day!","Tomorrow marks 30 days. You have earned this."},
        {"You did it! 30 days!",        "30 days. You have proven it — you can get through anything. You are unstoppable!"},
    };

    public HealingNotificationWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        if (!prefs.getBoolean(PREF_ACTIVE, false)) return Result.success();

        int dayCount = prefs.getInt(PREF_DAY_COUNT, 0) + 1;
        prefs.edit().putInt(PREF_DAY_COUNT, dayCount).apply();

        if (dayCount > 30) {
            prefs.edit().putBoolean(PREF_ACTIVE, false).apply();
            cancel(ctx);
            showFinalNotification(ctx);
            return Result.success();
        }

        int msgIndex = Math.min(dayCount - 1, HEALING_MESSAGES.length - 1);
        showDailyNotification(ctx, dayCount,
            HEALING_MESSAGES[msgIndex][0],
            HEALING_MESSAGES[msgIndex][1]);
        Log.d(TAG, "Healing day " + dayCount + " notification sent");
        return Result.success();
    }

    private void showDailyNotification(Context ctx, int day, String title, String message) {
        NotificationManager nm =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                CH_ID, "Healing Journey", NotificationManager.IMPORTANCE_DEFAULT);
            ch.setDescription("Your 30-day healing journey");
            nm.createNotificationChannel(ch);
        }
        Intent intent = new Intent(ctx, BreakupModeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        nm.notify(3000 + day, new NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Day " + day + "/30 — " + title)
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(pi)
            .setAutoCancel(true)
            .setColor(0xFF7C6FFF)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build());
    }

    private void showFinalNotification(Context ctx) {
        NotificationManager nm =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                CH_ID, "Healing Journey", NotificationManager.IMPORTANCE_HIGH));
        }
        String msg = "You completed 30 days! You have proven that you are unstoppable. " +
                     "A new chapter begins. The best is yet to come.";
        nm.notify(3099, new NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("You did it! You are stronger now!")
            .setContentText(msg)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
            .setAutoCancel(true)
            .setColor(0xFF7C6FFF)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build());
    }

    public static void schedule(Context ctx) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
            .putInt(PREF_DAY_COUNT, 0)
            .putBoolean(PREF_ACTIVE, true)
            .putLong(PREF_START_DAY, System.currentTimeMillis())
            .apply();
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                HealingNotificationWorker.class, 24, TimeUnit.HOURS)
            .addTag(WORK_TAG).build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            WORK_TAG, ExistingPeriodicWorkPolicy.UPDATE, req);
        Log.d(TAG, "HealingWorker scheduled for 30 days");
    }

    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WORK_TAG);
        PreferenceManager.getDefaultSharedPreferences(ctx)
            .edit().putBoolean(PREF_ACTIVE, false).apply();
    }
}