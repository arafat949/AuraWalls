package com.aurawalls.app.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class PexelsVideoResponse {
    @SerializedName("videos") public List<PexelsVideo> videos;
    public static class PexelsVideo {
        @SerializedName("id") public int id;
        @SerializedName("width") public int width;
        @SerializedName("height") public int height;
        @SerializedName("duration") public int duration;
        @SerializedName("image") public String image;
        @SerializedName("user") public PexelsUser user;
        @SerializedName("video_files") public List<PexelsVideoFile> videoFiles;
        public VideoModel toVideoModel() {
            String mp4Url = "";
            int bestWidth = 0;
            if (videoFiles != null) {
                // SD quality বেছে নাও — fast load হবে
                for (PexelsVideoFile f : videoFiles) {
                    if (f.link == null || f.link.isEmpty()) continue;
                    if (!"video/mp4".equals(f.fileType)) continue;
                    // 720p বা তার নিচে prefer করো
                    if (f.width >= 360 && f.width <= 1280) {
                        if (mp4Url.isEmpty() || f.width > bestWidth) {
                            mp4Url = f.link;
                            bestWidth = f.width;
                        }
                    }
                }
                // কিছু না পেলে যেকোনো mp4
                if (mp4Url.isEmpty()) {
                    for (PexelsVideoFile f : videoFiles) {
                        if (f.link != null && !f.link.isEmpty() && "video/mp4".equals(f.fileType)) {
                            mp4Url = f.link;
                            break;
                        }
                    }
                }
                // শেষ চেষ্টা
                if (mp4Url.isEmpty() && !videoFiles.isEmpty() && videoFiles.get(0).link != null) {
                    mp4Url = videoFiles.get(0).link;
                }
            }
            return new VideoModel(String.valueOf(id), mp4Url, image, user!=null?user.name:"Unknown", "Pexels", width, height, duration);
        }
    }
    public static class PexelsUser { @SerializedName("name") public String name; }
    public static class PexelsVideoFile {
        @SerializedName("link") public String link;
        @SerializedName("file_type") public String fileType;
        @SerializedName("width") public int width;
        @SerializedName("height") public int height;
    }
}
