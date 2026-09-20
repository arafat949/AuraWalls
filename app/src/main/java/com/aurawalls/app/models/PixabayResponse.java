package com.aurawalls.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Response wrapper for Pixabay GET /api/ */
public class PixabayResponse {
    @SerializedName("totalHits") public int totalHits;
    @SerializedName("hits")      public List<PixabayHit> hits;

    public static class PixabayHit {
        @SerializedName("id")            public int id;
        @SerializedName("imageWidth")    public int imageWidth;
        @SerializedName("imageHeight")   public int imageHeight;
        @SerializedName("user")          public String user;
        @SerializedName("pageURL")       public String pageURL;
        @SerializedName("webformatURL")  public String webformatURL;   // medium
        @SerializedName("largeImageURL") public String largeImageURL;  // large
        @SerializedName("fullHDURL")     public String fullHDURL;      // full (needs API approval)

        public WallpaperModel toWallpaperModel() {
            String full = (fullHDURL != null && !fullHDURL.isEmpty()) ? fullHDURL : largeImageURL;
            return new WallpaperModel(
                    String.valueOf(id),
                    webformatURL,
                    full,
                    user,
                    pageURL,
                    "Pixabay",
                    pageURL,
                    imageWidth, imageHeight
            );
        }
    }
}
