package com.aurawalls.app.models;

import java.io.Serializable;

/**
 * Unified wallpaper model that normalises data from Pexels, Unsplash, and Pixabay.
 */
public class WallpaperModel implements Serializable {

    private String id;
    private String thumbnailUrl;   // Medium-res for grid
    private String fullUrl;        // Original / large for download & set
    private String photographerName;
    private String photographerUrl;
    private String sourceName;     // "Pexels" | "Unsplash" | "Pixabay"
    private String sourceUrl;
    private int width;
    private int height;

    public WallpaperModel() {}

    public WallpaperModel(String id, String thumbnailUrl, String fullUrl,
                          String photographerName, String photographerUrl,
                          String sourceName, String sourceUrl,
                          int width, int height) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.fullUrl = fullUrl;
        this.photographerName = photographerName;
        this.photographerUrl = photographerUrl;
        this.sourceName = sourceName;
        this.sourceUrl = sourceUrl;
        this.width = width;
        this.height = height;
    }

    // ---------- Getters ----------
    public String getId()               { return id; }
    public String getThumbnailUrl()     { return thumbnailUrl; }
    public String getFullUrl()          { return fullUrl; }
    public String getPhotographerName() { return photographerName; }
    public String getPhotographerUrl()  { return photographerUrl; }
    public String getSourceName()       { return sourceName; }
    public String getSourceUrl()        { return sourceUrl; }
    public int    getWidth()            { return width; }
    public int    getHeight()           { return height; }

    // ---------- Setters ----------
    public void setId(String id)                           { this.id = id; }
    public void setThumbnailUrl(String thumbnailUrl)       { this.thumbnailUrl = thumbnailUrl; }
    public void setFullUrl(String fullUrl)                 { this.fullUrl = fullUrl; }
    public void setPhotographerName(String name)           { this.photographerName = name; }
    public void setPhotographerUrl(String url)             { this.photographerUrl = url; }
    public void setSourceName(String sourceName)           { this.sourceName = sourceName; }
    public void setSourceUrl(String sourceUrl)             { this.sourceUrl = sourceUrl; }
    public void setWidth(int width)                        { this.width = width; }
    public void setHeight(int height)                      { this.height = height; }

    /** Attribution string required by Pexels / Unsplash / Pixabay ToS */
    public String getAttributionText() {
        return "Photo by " + photographerName + " on " + sourceName;
    }
}
