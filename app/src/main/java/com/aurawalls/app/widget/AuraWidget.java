package com.aurawalls.app.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

import androidx.preference.PreferenceManager;

import com.aurawalls.app.R;
import com.aurawalls.app.activities.MainActivity;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.network.WallpaperRepository;
import com.aurawalls.app.utils.MoodHelper;
import com.bumptech.glide.Glide;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuraWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            updateWidget(context, manager, widgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_aura);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String auraTitle = prefs.getString("aura_title", "Cosmic Explorer");
        String auraType  = prefs.getString("aura_type",  "COSMIC_EXPLORER");
        String auraMood  = prefs.getString("aura_mood",  "Aesthetic");
        String emoji     = getEmojiForType(auraType);

        // Set text
        views.setTextViewText(R.id.widget_tv_aura, emoji + "  " + auraTitle);
        views.setTextViewText(R.id.widget_tv_mood, "Mood: " + auraMood);

        // Click → open MainActivity
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pi);

        // Refresh button
        Intent refreshIntent = new Intent(context, AuraWidget.class);
        refreshIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{widgetId});
        PendingIntent refreshPi = PendingIntent.getBroadcast(context, widgetId, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPi);

        manager.updateAppWidget(widgetId, views);

        // Load wallpaper image in background
        loadWidgetImage(context, manager, widgetId, views, auraMood);
    }

    private static void loadWidgetImage(Context context, AppWidgetManager manager,
                                         int widgetId, RemoteViews views, String mood) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String query = MoodHelper.getQuery(mood);
            WallpaperRepository.fetchWallpapers(query, 1,
                new WallpaperRepository.OnWallpapersLoaded() {
                    @Override
                    public void onSuccess(List<WallpaperModel> wallpapers) {
                        if (wallpapers.isEmpty()) return;
                        WallpaperModel pick = wallpapers.get(
                            new Random().nextInt(wallpapers.size()));
                        try {
                            Bitmap bmp = Glide.with(context)
                                .asBitmap()
                                .load(pick.getThumbnailUrl())
                                .submit(400, 200)
                                .get();
                            views.setImageViewBitmap(R.id.widget_iv_wallpaper, bmp);
                            manager.updateAppWidget(widgetId, views);
                        } catch (Exception e) {
                            android.util.Log.e("AuraWidget", "Image load failed: " + e.getMessage());
                        }
                    }
                    @Override public void onEndReached() {}
                    @Override public void onError(String msg) {}
                });
        });
    }

    private static String getEmojiForType(String type) {
        switch (type) {
            case "MIDNIGHT_DREAMER": return "🌙";
            case "GOLDEN_HOUR_SOUL": return "🌅";
            case "NEON_WANDERER":    return "⚡";
            case "ZEN_MASTER":       return "🍃";
            case "COSMIC_EXPLORER":  return "🚀";
            case "WILD_HEART":       return "🌿";
            case "URBAN_LEGEND":     return "🏙️";
            default:                 return "🌸";
        }
    }
}
