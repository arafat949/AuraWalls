package com.aurawalls.app.network;
import android.util.Log;
import com.aurawalls.app.BuildConfig;
import com.aurawalls.app.models.PexelsVideoResponse;
import com.aurawalls.app.models.PixabayVideoResponse;
import com.aurawalls.app.models.VideoModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VideoRepository {
    private static final String TAG = "VideoRepository";
    private static final int MAX_PAGE = 10;

    // In-memory cache: "query_page" -> List<VideoModel>
    private static final Map<String, List<VideoModel>> cache = new HashMap<>();

    public interface OnVideosLoaded {
        void onSuccess(List<VideoModel> videos);
        void onEndReached();
        void onError(String message);
    }

    public static void fetchVideos(String query, int page, OnVideosLoaded callback) {
        if (page > MAX_PAGE) { callback.onEndReached(); return; }

        // Check cache first — instant response
        String cacheKey = query + "_" + page;
        if (cache.containsKey(cacheKey)) {
            callback.onSuccess(new ArrayList<>(cache.get(cacheKey)));
            return;
        }

        List<VideoModel> merged = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(2);
        AtomicInteger errors = new AtomicInteger(0);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                List<VideoModel> valid = new ArrayList<>();
                for (VideoModel v : merged)
                    if (v.getVideoUrl() != null && !v.getVideoUrl().isEmpty()) valid.add(v);
                Collections.shuffle(valid);
                if (valid.isEmpty()) {
                    if (errors.get() >= 2) callback.onError("Network error.");
                    else callback.onEndReached();
                } else {
                    // Save to cache
                    cache.put(cacheKey, new ArrayList<>(valid));
                    callback.onSuccess(valid);
                }
            }
        };

        ApiClient.getPexelsApi().searchVideos(
            BuildConfig.PEXELS_API_KEY, query, 12, page, "portrait")
            .enqueue(new Callback<PexelsVideoResponse>() {
                @Override public void onResponse(Call<PexelsVideoResponse> call, Response<PexelsVideoResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().videos != null)
                        for (PexelsVideoResponse.PexelsVideo v : r.body().videos)
                            merged.add(v.toVideoModel());
                    checkDone.run();
                }
                @Override public void onFailure(Call<PexelsVideoResponse> call, Throwable t) {
                    Log.e(TAG, t.getMessage()); errors.incrementAndGet(); checkDone.run();
                }
            });

        ApiClient.getPixabayApi().searchVideos(
            BuildConfig.PIXABAY_API_KEY, query, 12, page, "all", "vertical", true)
            .enqueue(new Callback<PixabayVideoResponse>() {
                @Override public void onResponse(Call<PixabayVideoResponse> call, Response<PixabayVideoResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().hits != null)
                        for (PixabayVideoResponse.PixabayVideoHit h : r.body().hits)
                            merged.add(h.toVideoModel());
                    checkDone.run();
                }
                @Override public void onFailure(Call<PixabayVideoResponse> call, Throwable t) {
                    Log.e(TAG, t.getMessage()); errors.incrementAndGet(); checkDone.run();
                }
            });
    }


    public interface OnVideoItemReady { void onItem(VideoModel video); }
    public interface OnAllDone { void onDone(); }
    public interface OnError { void onError(String msg); }

    public static void fetchVideosProgressive(String query, int page,
            OnVideoItemReady onItem, OnAllDone onDone, OnError onError) {
        if (page > MAX_PAGE) { onDone.onDone(); return; }

        String cacheKey = query + "_" + page;
        if (cache.containsKey(cacheKey)) {
            for (VideoModel v : cache.get(cacheKey)) onItem.onItem(v);
            onDone.onDone();
            return;
        }

        List<VideoModel> allItems = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger pending = new AtomicInteger(2);

        Runnable checkDone = () -> {
            if (pending.decrementAndGet() == 0) {
                cache.put(cacheKey, new ArrayList<>(allItems));
                onDone.onDone();
            }
        };

        ApiClient.getPexelsApi().searchVideos(
            BuildConfig.PEXELS_API_KEY, query, 12, page, "portrait")
            .enqueue(new Callback<PexelsVideoResponse>() {
                @Override public void onResponse(Call<PexelsVideoResponse> call, Response<PexelsVideoResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().videos != null) {
                        for (PexelsVideoResponse.PexelsVideo v : r.body().videos) {
                            VideoModel m = v.toVideoModel();
                            if (m.getVideoUrl() != null && !m.getVideoUrl().isEmpty()) {
                                allItems.add(m);
                                onItem.onItem(m); // immediately deliver
                            }
                        }
                    }
                    checkDone.run();
                }
                @Override public void onFailure(Call<PexelsVideoResponse> call, Throwable t) {
                    Log.e(TAG, t.getMessage()); checkDone.run();
                }
            });

        ApiClient.getPixabayApi().searchVideos(
            BuildConfig.PIXABAY_API_KEY, query, 12, page, "all", "vertical", true)
            .enqueue(new Callback<PixabayVideoResponse>() {
                @Override public void onResponse(Call<PixabayVideoResponse> call, Response<PixabayVideoResponse> r) {
                    if (r.isSuccessful() && r.body() != null && r.body().hits != null) {
                        for (PixabayVideoResponse.PixabayVideoHit h : r.body().hits) {
                            VideoModel m = h.toVideoModel();
                            if (m.getVideoUrl() != null && !m.getVideoUrl().isEmpty()) {
                                allItems.add(m);
                                onItem.onItem(m); // immediately deliver
                            }
                        }
                    }
                    checkDone.run();
                }
                @Override public void onFailure(Call<PixabayVideoResponse> call, Throwable t) {
                    Log.e(TAG, t.getMessage()); checkDone.run();
                }
            });
    }
    public static void clearCache() { cache.clear(); }
}
