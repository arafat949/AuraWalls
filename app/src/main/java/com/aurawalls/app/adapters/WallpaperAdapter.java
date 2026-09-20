package com.aurawalls.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurawalls.app.R;
import com.aurawalls.app.activities.WallpaperDetailActivity;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.utils.FavoritesManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class WallpaperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_WALLPAPER = 0;
    private static final int TYPE_AD        = 1;
    private static final int AD_EVERY       = 8;

    private final List<WallpaperModel> items;
    private final Context context;
    private int lastAnimatedPosition = -1;

    private static final RequestOptions GLIDE_OPTS = new RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false);

    public WallpaperAdapter(Context context, List<WallpaperModel> items) {
        this.context = context;
        this.items   = items;
    }

    @Override
    public int getItemViewType(int position) {
        return (position > 0 && position % AD_EVERY == 0) ? TYPE_AD : TYPE_WALLPAPER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(context);
        if (viewType == TYPE_AD) {
            View v = inf.inflate(R.layout.item_native_ad, parent, false);
            return new AdViewHolder(v);
        }
        View v = inf.inflate(R.layout.item_wallpaper, parent, false);
        return new WallpaperViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AdViewHolder) return;

        WallpaperViewHolder h   = (WallpaperViewHolder) holder;
        WallpaperModel item     = items.get(position);
        FavoritesManager favMgr = FavoritesManager.getInstance(context);

        // ── Image Loading ──────────────────────────────────────────
        Glide.with(context)
                .load(item.getThumbnailUrl())
                .apply(GLIDE_OPTS)
                .thumbnail(
                    Glide.with(context)
                        .load(item.getThumbnailUrl()
                            + "?auto=compress&cs=tinysrgb&w=40")
                        .apply(GLIDE_OPTS)
                )
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .placeholder(R.drawable.placeholder_wallpaper)
                .into(h.ivThumb);

        // ── Attribution ────────────────────────────────────────────
        h.tvAttribution.setText(item.getAttributionText());

        // ── Favorite Button ────────────────────────────────────────
        updateFavoriteIcon(h.ivFavorite, favMgr.isFavorite(item.getId()));

        h.ivFavorite.setOnClickListener(v -> {
            boolean nowAdded = favMgr.toggle(item);
            updateFavoriteIcon(h.ivFavorite, nowAdded);
            if (nowAdded) {
                // Bounce animation on add
                h.ivFavorite.animate()
                    .scaleX(1.3f).scaleY(1.3f).setDuration(150)
                    .withEndAction(() ->
                        h.ivFavorite.animate()
                            .scaleX(1f).scaleY(1f).setDuration(150).start()
                    ).start();
            }
        });

        // ── Card tap → detail ──────────────────────────────────────
        h.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, WallpaperDetailActivity.class);
            intent.putExtra("wallpaper", item);
            context.startActivity(intent);
        });

        // ── Slide-up animation ─────────────────────────────────────
        if (position > lastAnimatedPosition) {
            h.itemView.startAnimation(
                AnimationUtils.loadAnimation(context, android.R.anim.fade_in));
            lastAnimatedPosition = position;
        }
    }

    private void updateFavoriteIcon(ImageView iv, boolean isFav) {
        iv.setImageResource(isFav
            ? android.R.drawable.btn_star_big_on
            : android.R.drawable.btn_star_big_off);
    }

    @Override
    public int getItemCount() { return items.size(); }

    public void addItems(List<WallpaperModel> newItems) {
        int start = items.size();
        items.addAll(newItems);
        notifyItemRangeInserted(start, newItems.size());
    }

    // ── ViewHolders ────────────────────────────────────────────────

    static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView ivThumb, ivFavorite;
        TextView  tvAttribution;

        WallpaperViewHolder(View v) {
            super(v);
            ivThumb       = v.findViewById(R.id.iv_thumb);
            ivFavorite    = v.findViewById(R.id.iv_favorite);
            tvAttribution = v.findViewById(R.id.tv_attribution);
        }
    }

    static class AdViewHolder extends RecyclerView.ViewHolder {
        AdViewHolder(View v) { super(v); }
    }
}
