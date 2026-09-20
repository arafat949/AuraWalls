package com.aurawalls.app.network;

import android.util.Log;

import com.aurawalls.app.BuildConfig;
import com.aurawalls.app.models.PexelsResponse;
import com.aurawalls.app.models.PixabayResponse;
import com.aurawalls.app.models.UnsplashResponse;
import com.aurawalls.app.models.WallpaperModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WallpaperRepository {

    private static final String TAG = "WallpaperRepository";
    private static final int MAX_PAGE = 10; // stop at page 10

    public interface OnWallpapersLoaded {
        void onSuccess(List<WallpaperModel> wallpapers);
        void onEndReached(); // no more results
        void onError(String message);
    }

    public static void fetchWallpapers(String query, int page, OnWallpapersLoaded callback) {
        // Stop if beyond max page
        if (page > MAX_PAGE) {
            callback.onEndReached();
            return;
        }

        List<WallpaperModel> merged  = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger        pending = new AtomicInteger(3);
        AtomicInteger        errors  = new AtomicInteger(0);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                if (merged.isEmpty()) {
                    if (errors.get() == 3) {
                        callback.onError("Network error. Please check your connection.");
                    } else {
                        callback.onEndReached(); // all APIs returned empty = end
                    }
                } else {
                    Collections.shuffle(merged);
                    callback.onSuccess(merged);
                }
            }
        };

        // 1. Pexels
        ApiClient.getPexelsApi().searchPhotos(
                BuildConfig.PEXELS_API_KEY, query, 15, page, "portrait"
        ).enqueue(new Callback<PexelsResponse>() {
            @Override
            public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (PexelsResponse.PexelsPhoto p : response.body().photos)
                        merged.add(p.toWallpaperModel());
                checkDone.run();
            }
            @Override
            public void onFailure(Call<PexelsResponse> call, Throwable t) {
                Log.e(TAG, "Pexels: " + t.getMessage());
                errors.incrementAndGet();
                checkDone.run();
            }
        });

        // 2. Unsplash
        ApiClient.getUnsplashApi().searchPhotos(
                "Client-ID " + BuildConfig.UNSPLASH_API_KEY, query, 15, page, "portrait"
        ).enqueue(new Callback<UnsplashResponse>() {
            @Override
            public void onResponse(Call<UnsplashResponse> call, Response<UnsplashResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (UnsplashResponse.UnsplashPhoto p : response.body().results)
                        merged.add(p.toWallpaperModel());
                checkDone.run();
            }
            @Override
            public void onFailure(Call<UnsplashResponse> call, Throwable t) {
                Log.e(TAG, "Unsplash: " + t.getMessage());
                errors.incrementAndGet();
                checkDone.run();
            }
        });

        // 3. Pixabay
        ApiClient.getPixabayApi().searchPhotos(
                BuildConfig.PIXABAY_API_KEY, query, 15, page, "photo", "vertical", true
        ).enqueue(new Callback<PixabayResponse>() {
            @Override
            public void onResponse(Call<PixabayResponse> call, Response<PixabayResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    for (PixabayResponse.PixabayHit h : response.body().hits)
                        merged.add(h.toWallpaperModel());
                checkDone.run();
            }
            @Override
            public void onFailure(Call<PixabayResponse> call, Throwable t) {
                Log.e(TAG, "Pixabay: " + t.getMessage());
                errors.incrementAndGet();
                checkDone.run();
            }
        });
    }
}
