package com.aurawalls.app.activities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurawalls.app.R;
import com.aurawalls.app.adapters.VideoAdapter;
import com.aurawalls.app.models.VideoModel;
import com.aurawalls.app.network.VideoRepository;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

public class VideoPlayerDialog extends BottomSheetDialogFragment {

    private static final String ARG_VIDEO    = "video";
    private static final String ARG_QUERY    = "query";

    private VideoModel video;
    private String     relatedQuery;

    private ExoPlayer  player;
    private PlayerView playerView;
    private ImageView  ivThumb, ivMute, ivClose;
    private TextView   tvAttribution, tvDuration;
    private MaterialButton btnSet, btnDownload;
    private RecyclerView   rvRelated;
    private View loadingSpinner;

    private boolean isMuted = false; // Sound on
    private VideoAdapter relatedAdapter;
    private List<VideoModel> relatedVideos = new ArrayList<>();

    public static VideoPlayerDialog newInstance(VideoModel video, String query) {
        VideoPlayerDialog f = new VideoPlayerDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VIDEO, video);
        args.putString(ARG_QUERY, query);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.VideoBottomSheetStyle);
        if (getArguments() != null) {
            video        = (VideoModel) getArguments().getSerializable(ARG_VIDEO);
            relatedQuery = getArguments().getString(ARG_QUERY, "nature");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_video_player, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Expand BottomSheet smoothly
        if (getDialog() instanceof BottomSheetDialog) {
            BottomSheetDialog bsd = (BottomSheetDialog) getDialog();
            bsd.getBehavior().setState(BottomSheetBehavior.STATE_EXPANDED);
            bsd.getBehavior().setSkipCollapsed(true);
            bsd.getBehavior().setDraggable(true);
            bsd.getBehavior().setPeekHeight(0);
            // Animation speed
            if (getDialog().getWindow() != null) {
                getDialog().getWindow().setWindowAnimations(
                    android.R.style.Animation_Dialog);
            }
        }

        bindViews(view);
        setupPlayer();
        setupButtons();
        loadRelatedVideos();
    }

    private void bindViews(View view) {
        playerView     = view.findViewById(R.id.dialog_player_view);
        ivThumb        = view.findViewById(R.id.dialog_iv_thumb);
        ivMute         = view.findViewById(R.id.dialog_iv_mute);
        ivClose        = view.findViewById(R.id.dialog_iv_close);
        tvAttribution  = view.findViewById(R.id.dialog_tv_attribution);
        tvDuration     = view.findViewById(R.id.dialog_tv_duration);
        btnSet         = view.findViewById(R.id.dialog_btn_set);
        btnDownload    = view.findViewById(R.id.dialog_btn_download);
        rvRelated      = view.findViewById(R.id.dialog_rv_related);
        loadingSpinner = view.findViewById(R.id.dialog_loading);

        // Thumbnail
        Glide.with(requireContext())
                .load(video.getThumbnailUrl())
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(ivThumb);

        tvAttribution.setText(video.getAttributionText());
        tvDuration.setText(video.getDurationFormatted());

        // Close button
        ivClose.setOnClickListener(v -> dismiss());
    }

    private void setupPlayer() {
        // Thumbnail সাথে সাথে দেখাও — hang feel দূর হবে
        ivThumb.setVisibility(View.VISIBLE);
        playerView.setVisibility(View.GONE);
        if (loadingSpinner != null) loadingSpinner.setVisibility(View.VISIBLE);

        // Background thread এ player setup করো
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;
            player = new ExoPlayer.Builder(requireContext())
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(15000)
                    .build();
            playerView.setPlayer(player);
            player.setRepeatMode(Player.REPEAT_MODE_ONE);
            player.setVolume(1f);
            playerView.setControllerHideOnTouch(true);


            player.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int state) {
                    if (state == Player.STATE_READY) {
                        ivThumb.setVisibility(View.GONE);
                        playerView.setVisibility(View.VISIBLE);
                        if (loadingSpinner != null)
                            loadingSpinner.setVisibility(View.GONE);
                    }
                }
            });

            player.setMediaItem(MediaItem.fromUri(video.getVideoUrl()));
            player.prepare();
            player.play();
            playerView.setUseController(true);
            playerView.setControllerAutoShow(true);
            playerView.setControllerHideOnTouch(true);

            // Hide settings button
            View settingsBtn = playerView.findViewById(androidx.media3.ui.R.id.exo_settings);
            if (settingsBtn != null) settingsBtn.setVisibility(View.GONE);
            playerView.showController();
        }, 100); // 100ms delay — UI render হওয়ার সুযোগ পাবে

        // Mute toggle
        ivMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            player.setVolume(isMuted ? 0f : 1f);
            ivMute.setImageResource(isMuted
                ? android.R.drawable.ic_lock_silent_mode
                : android.R.drawable.ic_lock_silent_mode_off);
        });

        // Tap to pause/play
        playerView.setOnClickListener(v -> {
            if (player.isPlaying()) player.pause();
            else player.play();
        });
    }

    private void setupButtons() {
        btnSet.setOnClickListener(v ->
            Toast.makeText(requireContext(),
                "Video wallpaper coming soon!", Toast.LENGTH_SHORT).show());

        btnDownload.setOnClickListener(v ->
            Toast.makeText(requireContext(),
                "Downloading video...", Toast.LENGTH_SHORT).show());
    }

    private void loadRelatedVideos() {
        relatedAdapter = new VideoAdapter(requireContext(), relatedVideos);
        rvRelated.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRelated.setAdapter(relatedAdapter);
        rvRelated.setNestedScrollingEnabled(true);

        // Related video click → play in top player
        relatedAdapter.setOnVideoClickListener(clickedVideo -> {
            video = clickedVideo;
            // Update UI
            Glide.with(requireContext()).load(clickedVideo.getThumbnailUrl())
                .into(ivThumb);
            ivThumb.setVisibility(android.view.View.VISIBLE);
            playerView.setVisibility(android.view.View.GONE);
            tvAttribution.setText(clickedVideo.getAttributionText());
            tvDuration.setText(clickedVideo.getDurationFormatted());
            // Release old player and start new
            if (player != null) {
                player.stop();
                player.clearMediaItems();
            }
            if (player == null) {
                player = new ExoPlayer.Builder(requireContext())
                    .setSeekBackIncrementMs(5000)
                    .setSeekForwardIncrementMs(15000)
                    .build();
                playerView.setPlayer(player);
                player.setRepeatMode(androidx.media3.common.Player.REPEAT_MODE_ONE);
                player.setVolume(isMuted ? 0f : 1f);
            }
            player.addListener(new androidx.media3.common.Player.Listener() {
                @Override public void onPlaybackStateChanged(int state) {
                    if (state == androidx.media3.common.Player.STATE_READY) {
                        ivThumb.setVisibility(android.view.View.GONE);
                        playerView.setVisibility(android.view.View.VISIBLE);
                    }
                }
            });
            player.setMediaItem(androidx.media3.common.MediaItem.fromUri(clickedVideo.getVideoUrl()));
            player.prepare();
            player.play();
            // Re-attach player to playerView so controls work
            playerView.setPlayer(player);
            playerView.setUseController(true);
            playerView.setControllerAutoShow(true);
            playerView.showController();
            // Scroll to top
            rvRelated.scrollToPosition(0);
        });

        // Related videos থেকে current video বাদ দিয়ে দেখাবে
        VideoRepository.fetchVideos(relatedQuery, 1, new VideoRepository.OnVideosLoaded() {
            @Override
            public void onSuccess(List<VideoModel> items) {
                if (!isAdded()) return;
                requireActivity().runOnUiThread(() -> {
                    // Current video বাদ দাও
                    List<VideoModel> filtered = new ArrayList<>();
                    for (VideoModel v : items) {
                        if (!v.getVideoUrl().equals(video.getVideoUrl())) {
                            filtered.add(v);
                            if (filtered.size() >= 5) break; // max 5 related
                        }
                    }
                    relatedVideos.addAll(filtered);
                    relatedAdapter.notifyDataSetChanged();
                });
            }
            @Override public void onEndReached() {}
            @Override public void onError(String message) {}
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
        if (relatedAdapter != null) {
            relatedAdapter.stopAll();
        }
    }
}
