package com.aurawalls.app.services;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
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

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * WeatherWallpaperWorker — আবহাওয়া অনুযায়ী wallpaper change করে।
 * প্রতি ৩ ঘণ্টায় weather check করে, change হলে নতুন wallpaper set করে।
 * WeatherThemeHelper এর logic use করে।
 */
public class WeatherWallpaperWorker extends Worker {

    private static final String TAG      = "WeatherWallpaperWorker";
    public  static final String WORK_TAG = "aura_weather_wallpaper";
    private static final String CH_ID    = "aura_weather_ch";
    public  static final String PREF_ENABLED  = "weather_wallpaper_enabled";
    public  static final String PREF_CITY     = "weather_city";
    public  static final String PREF_OWM_KEY  = "owm_api_key";

    // Default free OpenWeatherMap key — user settings থেকে override হবে
    private static final String DEFAULT_OWM_KEY = "bd5e378503939ddaee76f12ad7a97608";

    public WeatherWallpaperWorker(@NonNull Context ctx, @NonNull WorkerParameters params) {
        super(ctx, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        if (!prefs.getBoolean(PREF_ENABLED, false)) return Result.success();

        String city   = prefs.getString(PREF_CITY, "Dhaka");
        String owmKey = prefs.getString(PREF_OWM_KEY, DEFAULT_OWM_KEY);

        // Weather fetch
        String[] weatherData = fetchWeather(city, owmKey);
        String weatherMain = weatherData[0]; // e.g. "Rain"
        String temp        = weatherData[1]; // e.g. "28.5"
        String mood        = weatherData[2]; // e.g. "Calm"
        String reason      = weatherData[3]; // e.g. "🌧️ বৃষ্টির দিনে Calm wallpaper"

        // আগের weather এর সাথে same হলে change করো না
        String lastWeather = prefs.getString("weather_last_main", "");
        if (weatherMain.equals(lastWeather)) {
            Log.d(TAG, "Weather same as last — skipping");
            return Result.success();
        }

        String query = getQueryForWeather(weatherMain, temp);
        Log.d(TAG, "Weather: " + weatherMain + " | Mood: " + mood + " | Query: " + query);

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
                            .asBitmap().load(pick.getFullUrl()).submit().get();

                    String target = prefs.getString("auto_target", "BOTH");
                    success.set(WallpaperSetter.setWallpaper(ctx, bmp,
                            WallpaperSetter.Target.valueOf(target)));

                    if (success.get()) {
                        prefs.edit()
                            .putString("weather_last_main", weatherMain)
                            .putString("weather_last_mood", mood)
                            .putString("weather_last_reason", reason)
                            .putLong("weather_last_set", System.currentTimeMillis())
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

    // ── Weather API ───────────────────────────────────────────────────

    private String[] fetchWeather(String city, String key) {
        try {
            String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="
                    + city.replace(" ", "%20")
                    + "&appid=" + key + "&units=metric";
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == 200) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) sb.append(line);

                JSONObject json = new JSONObject(sb.toString());
                String weatherMain = json.getJSONArray("weather")
                        .getJSONObject(0).getString("main");
                double temp = json.getJSONObject("main").getDouble("temp");
                String tempStr = String.valueOf(temp);
                String mood   = weatherToMood(weatherMain, temp);
                String reason = weatherToReason(weatherMain, temp);
                return new String[]{weatherMain, tempStr, mood, reason};
            }
        } catch (Exception e) {
            Log.w(TAG, "Weather API failed: " + e.getMessage());
        }
        // Fallback to time-based
        String[] timeBased = SmartTimeWorker.getTimeBasedMood();
        return new String[]{"Clear", "25", timeBased[0], timeBased[1]};
    }

    private String weatherToMood(String weather, double temp) {
        switch (weather) {
            case "Rain":
            case "Drizzle":       return "Calm";
            case "Thunderstorm":  return "Dark";
            case "Snow":          return "Aesthetic";
            case "Clear":         return temp > 30 ? "Energetic" : "Nature";
            case "Clouds":        return "Focus";
            case "Fog":
            case "Mist":
            case "Haze":          return "Dark";
            default:              return "Aesthetic";
        }
    }

    private String weatherToReason(String weather, double temp) {
        switch (weather) {
            case "Rain":
            case "Drizzle":      return "🌧️ বৃষ্টির দিনে soothing wallpaper";
            case "Thunderstorm": return "⛈️ ঝড়ের রাতে dark wallpaper";
            case "Snow":         return "❄️ তুষারের জন্য aesthetic wallpaper";
            case "Clear":        return temp > 30 ? "☀️ গরমে energetic wallpaper" : "🌤️맑া আকাশে nature wallpaper";
            case "Clouds":       return "☁️ মেঘলায় minimal wallpaper";
            case "Fog":
            case "Mist":         return "🌫️ কুয়াশায় moody wallpaper";
            default:             return "🌈 আজকের আবহাওয়া অনুযায়ী wallpaper";
        }
    }

    private String getQueryForWeather(String weather, String tempStr) {
        double temp = 25;
        try { temp = Double.parseDouble(tempStr); } catch (Exception ignored) {}

        switch (weather) {
            case "Rain":
            case "Drizzle":      return "rainy street cozy aesthetic";
            case "Thunderstorm": return "dark storm dramatic lightning sky";
            case "Snow":         return "winter snow frost aesthetic";
            case "Clear":        return temp > 30 ? "bright summer sunny energetic" : "clear sky nature landscape";
            case "Clouds":       return "cloudy minimal moody landscape";
            case "Fog":
            case "Mist":
            case "Haze":         return "misty fog forest moody";
            default:             return "beautiful landscape nature";
        }
    }

    private void showNotification(Context ctx, String reason, String attribution) {
        NotificationManager nm =
            (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            nm.createNotificationChannel(new NotificationChannel(
                CH_ID, "Weather Wallpaper", NotificationManager.IMPORTANCE_LOW));
        }
        nm.notify(2002, new NotificationCompat.Builder(ctx, CH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("আবহাওয়া বদলেছে 🌤️")
            .setContentText(reason + " • " + attribution)
            .setAutoCancel(true)
            .build());
    }

    // ── Schedule / Cancel ─────────────────────────────────────────────

    public static void schedule(Context ctx) {
        PeriodicWorkRequest req = new PeriodicWorkRequest.Builder(
                WeatherWallpaperWorker.class, 3, TimeUnit.HOURS)
            .addTag(WORK_TAG)
            .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
            WORK_TAG,
            ExistingPeriodicWorkPolicy.UPDATE,
            req);
        Log.d(TAG, "WeatherWallpaperWorker scheduled ✅");
    }

    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelAllWorkByTag(WORK_TAG);
        Log.d(TAG, "WeatherWallpaperWorker cancelled");
    }
}
