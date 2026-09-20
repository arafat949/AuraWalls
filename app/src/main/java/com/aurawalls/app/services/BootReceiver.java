package com.aurawalls.app.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean autoEnabled = prefs.getBoolean("auto_enabled", false);
        if (!autoEnabled) return;

        int intervalHours = prefs.getInt("auto_interval_hours", 1);
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                AutoChangerWorker.class, intervalHours, TimeUnit.HOURS)
                .addTag(AutoChangerWorker.WORK_TAG)
                .build();
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                AutoChangerWorker.WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE,
                req);
    }
}
