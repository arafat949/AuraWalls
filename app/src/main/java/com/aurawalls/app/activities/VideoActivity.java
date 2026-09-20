package com.aurawalls.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.aurawalls.app.R;
import com.aurawalls.app.adapters.VideoAdapter;
import com.aurawalls.app.models.VideoModel;
import com.aurawalls.app.network.VideoRepository;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.ArrayList;
import java.util.List;

public class VideoActivity extends BaseActivity {
    private VideoAdapter adapter;
    private List<VideoModel> videos = new ArrayList<>();
    private LinearLayoutManager layoutManager;
    private LinearProgressIndicator progressBar;
    private String currentQuery = "nature";
    private int currentPage = 1;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        setupToolbar();
        setupMoodChips();
        setupRecyclerView();
        loadVideos(true);
    }

    private void setupToolbar() {
        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("🎬 Video Walls");
        }
    }

    private void setupMoodChips() {
        ChipGroup cg = findViewById(R.id.chip_group_video_moods);
        String[] videoMoods = {"🌿 Nature","🌊 Ocean","🌌 Space","🌆 City","🔥 Fire","🌅 Sunset","❄️ Winter","🎭 Aesthetic"};
        String[] videoQueries = {"nature","ocean waves","space galaxy","city night","fire flames","sunset","winter snow","aesthetic dark"};
        for (int i = 0; i < videoMoods.length; i++) {
            final String query = videoQueries[i];
            Chip chip = new Chip(this);
            chip.setText(videoMoods[i]);
            chip.setCheckable(true);
            if (i == 0) chip.setChecked(true);
            chip.setOnCheckedChangeListener((v, checked) -> {
                if (checked) { currentQuery = query; loadVideos(true); }
            });
            cg.addView(chip);
        }
    }

    private void setupRecyclerView() {
        RecyclerView rv = findViewById(R.id.rv_videos);
        layoutManager = new LinearLayoutManager(this);
        adapter = new VideoAdapter(this, videos);
        adapter.setOnVideoClickListener(video -> {
            VideoPlayerDialog dialog = VideoPlayerDialog.newInstance(video, currentQuery);
            dialog.show(getSupportFragmentManager(), "video_player");
        });
        progressBar = findViewById(R.id.progress_bar);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int last = layoutManager.findLastVisibleItemPosition();
                if (!isLoading && last >= adapter.getItemCount() - 4) {
                    currentPage++;
                    loadVideos(false);
                }
            }
        });
    }

    private void loadVideos(boolean reset) {
        if (isLoading) return;
        isLoading = true;
        if (reset) {
            currentPage = 1;
            adapter.stopAll();
            videos.clear();
            adapter.notifyDataSetChanged();
        }
        progressBar.setVisibility(View.VISIBLE);
        VideoRepository.fetchVideos(currentQuery, currentPage, new VideoRepository.OnVideosLoaded() {
            @Override public void onSuccess(List<VideoModel> items) {
                runOnUiThread(() -> {
                    adapter.addItems(items);
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                });
            }
            @Override public void onEndReached() {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                });
            }
            @Override public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(VideoActivity.this, message, Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                    isLoading = false;
                });
            }
        });
    }

    @Override protected void onPause() { super.onPause(); adapter.stopAll(); }
    @Override protected void onDestroy() { super.onDestroy(); adapter.stopAll(); }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
