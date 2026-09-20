package com.aurawalls.app.models;
import java.io.Serializable;
public class VideoModel implements Serializable {
    private String id, videoUrl, thumbnailUrl, photographerName, sourceName;
    private int width, height, duration;
    public VideoModel() {}
    public VideoModel(String id, String videoUrl, String thumbnailUrl, String photographerName, String sourceName, int width, int height, int duration) {
        this.id=id; this.videoUrl=videoUrl; this.thumbnailUrl=thumbnailUrl;
        this.photographerName=photographerName; this.sourceName=sourceName;
        this.width=width; this.height=height; this.duration=duration;
    }
    public String getId() { return id; }
    public String getVideoUrl() { return videoUrl; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getPhotographerName() { return photographerName; }
    public String getSourceName() { return sourceName; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getDuration() { return duration; }
    public void setVideoUrl(String u) { this.videoUrl=u; }
    public void setThumbnailUrl(String u) { this.thumbnailUrl=u; }
    public String getDurationFormatted() { return String.format("%d:%02d", duration/60, duration%60); }
    public String getAttributionText() { return "Video by "+photographerName+" on "+sourceName; }
}
