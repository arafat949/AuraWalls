package com.aurawalls.app.utils;

import android.content.Context;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Fetches current weather and time-of-day to recommend a wallpaper mood.
 * Uses OpenWeatherMap free API (no key needed for basic weather by city).
 * Falls back to time-based theme if network unavailable.
 */
public class WeatherThemeHelper {

    private static final String TAG = "WeatherThemeHelper";
    // Free OpenWeatherMap API — replace with your key for production
    private static final String OWM_KEY = "YOUR_OWM_KEY_HERE";

    public interface OnThemeReady {
        void onThemeReady(String mood, String reason);
    }

    /**
     * Determines the best wallpaper mood based on current time + weather.
     * Runs network call on background thread, delivers result on calling thread.
     */
    public static void getRecommendedMood(Context ctx, String cityName, OnThemeReady callback) {
        new Thread(() -> {
            try {
                String urlStr = "https://api.openweathermap.org/data/2.5/weather?q="
                        + cityName + "&appid=" + OWM_KEY + "&units=metric";
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

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

                    String mood   = weatherToMood(weatherMain, temp);
                    String reason = weatherToReason(weatherMain, temp);
                    callback.onThemeReady(mood, reason);
                    return;
                }
            } catch (Exception e) {
                Log.w(TAG, "Weather fetch failed, falling back to time: " + e.getMessage());
            }
            // Fallback: time-based
            String[] timeBased = timeBasedMood();
            callback.onThemeReady(timeBased[0], timeBased[1]);
        }).start();
    }

    private static String weatherToMood(String weather, double temp) {
        switch (weather) {
            case "Rain":
            case "Drizzle":
            case "Thunderstorm": return "Calm";
            case "Snow":         return "Aesthetic";
            case "Clear":
                return temp > 30 ? "Energetic" : "Nature";
            case "Clouds":       return "Focus";
            case "Fog":
            case "Mist":         return "Dark";
            default:             return "Aesthetic";
        }
    }

    private static String weatherToReason(String weather, double temp) {
        switch (weather) {
            case "Rain":         return "🌧️ বৃষ্টির দিনে Calm wallpaper";
            case "Snow":         return "❄️ তুষারের জন্য Aesthetic wallpaper";
            case "Clear":        return temp > 30 ? "☀️ গরমে Energetic wallpaper" : "🌤️맑은 আকাশে Nature wallpaper";
            case "Clouds":       return "☁️ মেঘলায় Focus wallpaper";
            default:             return "🌈 আজকের আবহাওয়া অনুযায়ী wallpaper";
        }
    }

    private static String[] timeBasedMood() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 9)   return new String[]{"Nature",   "🌅 সূর্যোদয়ের প্রকৃতি"};
        if (hour >= 9 && hour < 12)  return new String[]{"Energetic", "⚡ সকালের শক্তি"};
        if (hour >= 12 && hour < 15) return new String[]{"Focus",     "🎯 দুপুরের মনোযোগ"};
        if (hour >= 15 && hour < 18) return new String[]{"Aesthetic", "✨ বিকেলের সৌন্দর্য"};
        if (hour >= 18 && hour < 21) return new String[]{"Romantic",  "🌇 সন্ধ্যার রোমান্স"};
        return new String[]{"Dark", "🌙 রাতের নীরবতা"};
    }
}
