package com.aurawalls.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.aurawalls.app.R;
import com.aurawalls.app.models.VideoModel;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    public interface OnVideoClickListener {
        void onVideoClick(com.aurawalls.app.models.VideoModel video);
    }

    private OnVideoClickListener clickListener;

    public void setOnVideoClickListener(OnVideoClickListener listener) {
        this.clickListener = listener;
    }

    private final List<VideoModel> items;
    private final Context context;
    private VideoViewHolder currentlyPlaying = null;

    public VideoAdapter(Context context, List<VideoModel> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder h, int position) {
        VideoModel video = items.get(position);
        h.currentVideo = video;

        // Release previous player if recycled
        if (h.player != null) {
            h.player.release();
            h.player = null;
        }

        h.isPlaying = false;
        h.playerView.setVisibility(View.INVISIBLE);
        h.ivPlayPause.setVisibility(View.VISIBLE);
        h.ivPlayPause.setImageResource(android.R.drawable.ic_media_play);

        // Thumbnail
        Glide.with(context)
                .load(video.getThumbnailUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(h.ivThumb);

        h.tvAttribution.setText(video.getAttributionText());
        h.tvDuration.setText(video.getDurationFormatted());
        h.tvSourceBadge.setText("▶ " + video.getSourceName());

        // Entire card tap → popup player (YouTube style)
        h.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onVideoClick(video);
        });
        h.ivThumb.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onVideoClick(video);
        });
        h.ivPlayPause.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onVideoClick(video);
        });
        // PlayerView এ click করলেও popup
        h.playerView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onVideoClick(video);
        });

        // Mute toggle
        h.ivMute.setOnClickListener(v -> {
            if (h.player != null) {
                h.isMuted = !h.isMuted;
                h.player.setVolume(h.isMuted ? 0f : 1f);
                h.ivMute.setImageResource(h.isMuted
                        ? android.R.drawable.ic_lock_silent_mode
                        : android.R.drawable.ic_lock_silent_mode_off);
            }
        });

        h.btnSetWallpaper.setOnClickListener(v ->
                Toast.makeText(context, "Video wallpaper coming soon!", Toast.LENGTH_SHORT).show());
        h.btnDownload.setOnClickListener(v ->
                Toast.makeText(context, "Downloading…", Toast.LENGTH_SHORT).show());
    }

    private void togglePlay(VideoViewHolder h, VideoModel video) {
        if (h.isPlaying) {
            if (h.player != null) h.player.pause();
            h.isPlaying = false;
            h.ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
            h.ivPlayPause.setVisibility(View.VISIBLE);
        } else {
            startPlayForHolder(h);
        }
    }

    public void startPlayForHolder(VideoViewHolder h) {
        if (currentlyPlaying != null && currentlyPlaying != h) {
            stopHolder(currentlyPlaying);
        }
        if (h.currentVideo == null) return;

        // Build ExoPlayer
        if (h.player == null) {
            h.player = new ExoPlayer.Builder(context).build();
            h.playerView.setPlayer(h.player);
            h.player.setRepeatMode(Player.REPEAT_MODE_ONE);
            h.player.setVolume(1f); // sound on
            h.player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        h.playerView.setVisibility(View.VISIBLE);
                        h.ivPlayPause.setVisibility(View.GONE);
                        h.isPlaying = true;
                    }
                }
            });
        }

        h.player.setMediaItem(MediaItem.fromUri(h.currentVideo.getVideoUrl()));
        h.player.prepare();
        h.player.play();
        currentlyPlaying = h;
    }

    public void stopAll() {
        if (currentlyPlaying != null) {
            stopHolder(currentlyPlaying);
            currentlyPlaying = null;
        }
    }

    private void stopHolder(VideoViewHolder h) {
        if (h.player != null) {
            h.player.stop();
            h.player.release();
            h.player = null;
        }
        h.playerView.setVisibility(View.INVISIBLE);
        h.ivPlayPause.setImageResource(android.R.drawable.ic_media_play);
        h.ivPlayPause.setVisibility(View.VISIBLE);
        h.isPlaying = false;
    }

    @Override
    public void onViewRecycled(@NonNull VideoViewHolder h) {
        super.onViewRecycled(h);
        stopHolder(h);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void addItems(List<VideoModel> newItems) {
        int s = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(s, newItems.size());
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        public final ImageView ivThumb, ivPlayPause, ivMute;
        public final PlayerView playerView;
        public final TextView tvAttribution, tvDuration, tvSourceBadge;
        public final MaterialButton btnSetWallpaper, btnDownload;
        public ExoPlayer player = null;
        public boolean isPlaying = false, isMuted = true;
        public VideoModel currentVideo = null;

        public VideoViewHolder(View v) {
            super(v);
            ivThumb       = v.findViewById(R.id.iv_video_thumb);
            playerView    = v.findViewById(R.id.player_view);
            ivPlayPause   = v.findViewById(R.id.iv_play_pause);
            ivMute        = v.findViewById(R.id.iv_mute);
            tvAttribution = v.findViewById(R.id.tv_attribution);
            tvDuration    = v.findViewById(R.id.tv_duration);
            tvSourceBadge = v.findViewById(R.id.tv_source_badge);
            btnSetWallpaper = v.findViewById(R.id.btn_set_wallpaper);
            btnDownload   = v.findViewById(R.id.btn_download);
        }
    }
}
