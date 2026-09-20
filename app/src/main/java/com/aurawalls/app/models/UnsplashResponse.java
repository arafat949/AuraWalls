package com.aurawalls.app.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Response wrapper for Unsplash GET /search/photos */
public class UnsplashResponse {
    @SerializedName("results") public List<UnsplashPhoto> results;
    @SerializedName("total")   public int total;

    public static class UnsplashPhoto {
        @SerializedName("id")    public String id;
        @SerializedName("width") public int width;
        @SerializedName("height")public int height;
        @SerializedName("links") public Links links;
        @SerializedName("urls")  public Urls urls;
        @SerializedName("user")  public User user;

        public static class Links {
            @SerializedName("html") public String html;
        }
        public static class Urls {
            @SerializedName("raw")     public String raw;
            @SerializedName("full")    public String full;
            @SerializedName("regular") public String regular;
            @SerializedName("small")   public String small;
            @SerializedName("thumb")   public String thumb;
        }
        public static class User {
            @SerializedName("name") public String name;
            @SerializedName("links") public UserLinks links;
            public static class UserLinks {
                @SerializedName("html") public String html;
            }
        }

        public WallpaperModel toWallpaperModel() {
            return new WallpaperModel(
                    id,
                    urls != null ? urls.regular : "",
                    urls != null ? urls.full    : "",
                    user != null ? user.name    : "Unknown",
                    (user != null && user.links != null) ? user.links.html : "",
                    "Unsplash",
                    (links != null) ? links.html : "",
                    width, height
            );
        }
    }
}
