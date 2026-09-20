package com.aurawalls.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Response wrapper for Pexels /v1/search */
public class PexelsResponse {
    @SerializedName("photos") public List<PexelsPhoto> photos;
    @SerializedName("total_results") public int totalResults;
    @SerializedName("next_page") public String nextPage;

    public static class PexelsPhoto {
        @SerializedName("id")           public int id;
        @SerializedName("width")        public int width;
        @SerializedName("height")       public int height;
        @SerializedName("photographer") public String photographer;
        @SerializedName("photographer_url") public String photographerUrl;
        @SerializedName("url")          public String url;  // Pexels page URL
        @SerializedName("src")          public Src src;

        public static class Src {
            @SerializedName("original") public String original;
            @SerializedName("large2x")  public String large2x;
            @SerializedName("medium")   public String medium;
            @SerializedName("small")    public String small;
        }

        public WallpaperModel toWallpaperModel() {
            return new WallpaperModel(
                    String.valueOf(id),
                    src != null ? src.medium    : "",
                    src != null ? src.original  : "",
                    photographer,
                    photographerUrl,
                    "Pexels",
                    url,
                    width, height
            );
        }
    }
}
