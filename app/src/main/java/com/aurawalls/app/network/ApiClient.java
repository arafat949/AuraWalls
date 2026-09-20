package com.aurawalls.app.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    private static final String BASE_PEXELS   = "https://api.pexels.com/";
    private static final String BASE_UNSPLASH = "https://api.unsplash.com/";
    private static final String BASE_PIXABAY  = "https://pixabay.com/";

    private static PexelsApi   pexelsApi;
    private static UnsplashApi unsplashApi;
    private static PixabayApi  pixabayApi;

    private static OkHttpClient buildClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    private static Retrofit buildRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static PexelsApi getPexelsApi() {
        if (pexelsApi == null)
            pexelsApi = buildRetrofit(BASE_PEXELS).create(PexelsApi.class);
        return pexelsApi;
    }

    public static UnsplashApi getUnsplashApi() {
        if (unsplashApi == null)
            unsplashApi = buildRetrofit(BASE_UNSPLASH).create(UnsplashApi.class);
        return unsplashApi;
    }

    public static PixabayApi getPixabayApi() {
        if (pixabayApi == null)
            pixabayApi = buildRetrofit(BASE_PIXABAY).create(PixabayApi.class);
        return pixabayApi;
    }
}
