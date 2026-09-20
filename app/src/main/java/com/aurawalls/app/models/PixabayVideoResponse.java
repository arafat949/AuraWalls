package com.aurawalls.app.models;
import com.google.gson.annotations.SerializedName;
import java.util.List;
public class PixabayVideoResponse {
    @SerializedName("hits") public List<PixabayVideoHit> hits;
    public static class PixabayVideoHit {
        @SerializedName("id") public int id;
        @SerializedName("duration") public int duration;
        @SerializedName("userImageURL") public String userImageURL;
        @SerializedName("user") public String user;
        @SerializedName("videos") public PixabayVideoSizes videos;
        public VideoModel toVideoModel() {
            String mp4Url="", thumbUrl=""; int w=0,h=0;
            if (videos!=null) {
                PixabayVideoFile best=null;
                if (videos.medium!=null&&!videos.medium.url.isEmpty()) best=videos.medium;
                else if (videos.small!=null&&!videos.small.url.isEmpty()) best=videos.small;
                else if (videos.tiny!=null&&!videos.tiny.url.isEmpty()) best=videos.tiny;
                if (best!=null) { mp4Url=best.url; thumbUrl=best.thumbnail; w=best.width; h=best.height; }
            }
            if (thumbUrl==null||thumbUrl.isEmpty()) thumbUrl=userImageURL;
            return new VideoModel(String.valueOf(id), mp4Url, thumbUrl, user!=null?user:"Unknown", "Pixabay", w, h, duration);
        }
    }
    public static class PixabayVideoSizes {
        @SerializedName("medium") public PixabayVideoFile medium;
        @SerializedName("small") public PixabayVideoFile small;
        @SerializedName("tiny") public PixabayVideoFile tiny;
    }
    public static class PixabayVideoFile {
        @SerializedName("url") public String url;
        @SerializedName("width") public int width;
        @SerializedName("height") public int height;
        @SerializedName("thumbnail") public String thumbnail;
    }
}
