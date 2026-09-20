package com.aurawalls.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.aurawalls.app.models.WallpaperModel;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages favorite wallpapers using SharedPreferences + Gson serialization.
 * No Room database needed — lightweight and fast.
 */
public class FavoritesManager {

    private static final String PREFS_NAME = "aura_favorites";
    private static final String KEY_LIST   = "favorites_list";
    private static final int    MAX_FAVS   = 200;

    private static FavoritesManager instance;
    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    private FavoritesManager(Context ctx) {
        prefs = ctx.getApplicationContext()
                   .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static FavoritesManager getInstance(Context ctx) {
        if (instance == null) instance = new FavoritesManager(ctx);
        return instance;
    }

    public List<WallpaperModel> getAll() {
        String json = prefs.getString(KEY_LIST, "[]");
        Type type = new TypeToken<List<WallpaperModel>>(){}.getType();
        List<WallpaperModel> list = gson.fromJson(json, type);
        return list != null ? list : new ArrayList<>();
    }

    public boolean isFavorite(String wallpaperId) {
        for (WallpaperModel w : getAll())
            if (w.getId().equals(wallpaperId)) return true;
        return false;
    }

    public boolean toggle(WallpaperModel wallpaper) {
        List<WallpaperModel> list = getAll();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId().equals(wallpaper.getId())) {
                list.remove(i);
                save(list);
                return false; // removed
            }
        }
        if (list.size() < MAX_FAVS) {
            list.add(0, wallpaper); // add to top
            save(list);
        }
        return true; // added
    }

    public void remove(String wallpaperId) {
        List<WallpaperModel> list = getAll();
        list.removeIf(w -> w.getId().equals(wallpaperId));
        save(list);
    }

    public int count() { return getAll().size(); }

    private void save(List<WallpaperModel> list) {
        prefs.edit().putString(KEY_LIST, gson.toJson(list)).apply();
    }
}
