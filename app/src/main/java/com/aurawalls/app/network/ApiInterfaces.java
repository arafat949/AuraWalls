package com.aurawalls.app.network;
import com.aurawalls.app.models.PexelsResponse;
import com.aurawalls.app.models.PexelsVideoResponse;
import com.aurawalls.app.models.PixabayResponse;
import com.aurawalls.app.models.PixabayVideoResponse;
import com.aurawalls.app.models.UnsplashResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;
interface PexelsApi {
    @GET("v1/search")
    Call<PexelsResponse> searchPhotos(@Header("Authorization") String apiKey, @Query("query") String query, @Query("per_page") int perPage, @Query("page") int page, @Query("orientation") String orientation);
    @GET("videos/search")
    Call<PexelsVideoResponse> searchVideos(@Header("Authorization") String apiKey, @Query("query") String query, @Query("per_page") int perPage, @Query("page") int page, @Query("orientation") String orientation);
}
interface UnsplashApi {
    @GET("search/photos")
    Call<UnsplashResponse> searchPhotos(@Header("Authorization") String clientId, @Query("query") String query, @Query("per_page") int perPage, @Query("page") int page, @Query("orientation") String orientation);
}
interface PixabayApi {
    @GET("api/")
    Call<PixabayResponse> searchPhotos(@Query("key") String apiKey, @Query("q") String query, @Query("per_page") int perPage, @Query("page") int page, @Query("image_type") String imageType, @Query("orientation") String orientation, @Query("safesearch") boolean safeSearch);
    @GET("api/videos/")
    Call<PixabayVideoResponse> searchVideos(@Query("key") String apiKey, @Query("q") String query, @Query("per_page") int perPage, @Query("page") int page, @Query("video_type") String videoType, @Query("orientation") String orientation, @Query("safesearch") boolean safeSearch);
}
