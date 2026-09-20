package com.aurawalls.app.activities;

import android.app.WallpaperManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.aurawalls.app.R;
import com.aurawalls.app.models.WallpaperModel;
import com.aurawalls.app.utils.FavoritesManager;
import com.aurawalls.app.utils.ImageFilterHelper;
import com.aurawalls.app.utils.PaletteExtractor;
import com.aurawalls.app.utils.WallpaperDownloader;
import com.aurawalls.app.utils.WallpaperSetter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class WallpaperDetailActivity extends BaseActivity {

    private ImageView      ivPreview;
    private Bitmap         originalBitmap, previewBitmap;
    private WallpaperModel wallpaper;

    private SeekBar    sbBlur, sbBrightness, sbGrayscale;
    private View[]     paletteViews    = new View[5];
    private TextView[] paletteHexTexts = new TextView[5];

    private RewardedAd rewardedAd;
    private View       filterPanel;
    private ImageButton btnFavorite;
    private boolean    isFavorited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_detail);
        wallpaper = (WallpaperModel) getIntent().getSerializableExtra("wallpaper");
        if (wallpaper == null) { finish(); return; }
        bindViews();
        loadImage();
        loadRewardedAd();
        checkFavoriteStatus();
    }

    private void bindViews() {
        ivPreview    = findViewById(R.id.iv_preview);
        sbBlur       = findViewById(R.id.sb_blur);
        sbBrightness = findViewById(R.id.sb_brightness);
        sbGrayscale  = findViewById(R.id.sb_grayscale);
        filterPanel  = findViewById(R.id.filter_panel);
        btnFavorite  = findViewById(R.id.btn_favorite);

        ((TextView) findViewById(R.id.tv_attribution)).setText(wallpaper.getAttributionText());

        int[] palIds    = {R.id.pal1, R.id.pal2, R.id.pal3, R.id.pal4, R.id.pal5};
        int[] palHexIds = {R.id.pal1_hex, R.id.pal2_hex, R.id.pal3_hex, R.id.pal4_hex, R.id.pal5_hex};
        for (int i = 0; i < 5; i++) {
            paletteViews[i]    = findViewById(palIds[i]);
            paletteHexTexts[i] = findViewById(palHexIds[i]);
        }

        // Filters
        findViewById(R.id.btn_unlock_filters).setOnClickListener(v -> showRewardedAd());
        SeekBar.OnSeekBarChangeListener reapply = new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean u) { applyFilters(); }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        };
        sbBlur.setOnSeekBarChangeListener(reapply);
        sbBrightness.setOnSeekBarChangeListener(reapply);
        sbGrayscale.setOnSeekBarChangeListener(reapply);

        // Set Wallpaper button → Bottom Sheet
        findViewById(R.id.btn_set_both).setOnClickListener(v -> showSetWallpaperSheet());

        // Download
        findViewById(R.id.btn_download).setOnClickListener(v -> downloadWallpaper());

        // Favorite
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        // Share
        findViewById(R.id.btn_share).setOnClickListener(v -> shareWallpaper());
    }

    // ── Custom Set Wallpaper Bottom Sheet ─────────────────────────────

    private void showSetWallpaperSheet() {
        if (previewBitmap == null) {
            Toast.makeText(this, "Image still loading...", Toast.LENGTH_SHORT).show();
            return;
        }

        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.VideoBottomSheetStyle);
        View view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_set_wallpaper, null);
        sheet.setContentView(view);
        sheet.getBehavior().setPeekHeight(600);

        // Home Screen
        view.findViewById(R.id.option_home).setOnClickListener(v -> {
            sheet.dismiss();
            setWallpaperWithFeedback(WallpaperSetter.Target.HOME, "🏠 Home Screen");
        });

        // Lock Screen
        view.findViewById(R.id.option_lock).setOnClickListener(v -> {
            sheet.dismiss();
            setWallpaperWithFeedback(WallpaperSetter.Target.LOCK, "🔒 Lock Screen");
        });

        // Both
        view.findViewById(R.id.option_both).setOnClickListener(v -> {
            sheet.dismiss();
            setWallpaperWithFeedback(WallpaperSetter.Target.BOTH, "✨ Home + Lock Screen");
        });

        // System Picker
        view.findViewById(R.id.option_system).setOnClickListener(v -> {
            sheet.dismiss();
            openSystemWallpaperPicker();
        });

        sheet.show();
    }

    private void setWallpaperWithFeedback(WallpaperSetter.Target target, String label) {
        if (previewBitmap == null) return;

        // Loading toast
        Toast.makeText(this, "Setting wallpaper...", Toast.LENGTH_SHORT).show();

        Bitmap toSet = previewBitmap;
        new Thread(() -> {
            boolean ok = WallpaperSetter.setWallpaper(this, toSet, target);
            runOnUiThread(() -> {
                if (ok) {
                    // Stats update
                    android.content.SharedPreferences prefs =
                        androidx.preference.PreferenceManager.getDefaultSharedPreferences(this);
                    int count = prefs.getInt("stat_wallpapers_set", 0);
                    prefs.edit().putInt("stat_wallpapers_set", count + 1).apply();

                    // Success feedback
                    showSuccessSheet(label);
                } else {
                    Toast.makeText(this, "Failed to set wallpaper.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void showSuccessSheet(String label) {
        BottomSheetDialog sheet = new BottomSheetDialog(this, R.style.VideoBottomSheetStyle);
        View view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_wallpaper_set_success, null);

        if (view == null) {
            Toast.makeText(this, "✅ " + label + " wallpaper set!", Toast.LENGTH_LONG).show();
            return;
        }

        TextView tvMsg = view.findViewById(R.id.tv_success_msg);
        if (tvMsg != null) tvMsg.setText(label + " wallpaper set successfully!");

        MaterialButton btnClose = view.findViewById(R.id.btn_close_success);
        if (btnClose != null) btnClose.setOnClickListener(v -> sheet.dismiss());

        MaterialButton btnShare = view.findViewById(R.id.btn_share_success);
        if (btnShare != null) btnShare.setOnClickListener(v -> {
            sheet.dismiss();
            shareWallpaper();
        });

        sheet.setContentView(view);
        sheet.show();
    }

    private void openSystemWallpaperPicker() {
        try {
            // Save bitmap to cache first
            File cache = new File(getCacheDir(), "wallpaper_set.jpg");
            FileOutputStream fos = new FileOutputStream(cache);
            previewBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();

            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                this, getPackageName() + ".provider", cache);

            Intent intent = WallpaperManager.getInstance(this).getCropAndSetWallpaperIntent(uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            // Fallback — direct set
            Toast.makeText(this, "Opening system picker...", Toast.LENGTH_SHORT).show();
            setWallpaperWithFeedback(WallpaperSetter.Target.BOTH, "✨ Both Screens");
        }
    }

    // ── Favorites ─────────────────────────────────────────────────────

    private void checkFavoriteStatus() {
        isFavorited = FavoritesManager.getInstance(this).isFavorite(wallpaper.getId());
        updateFavoriteIcon();
    }

    private void toggleFavorite() {
        isFavorited = FavoritesManager.getInstance(this).toggle(wallpaper);
        updateFavoriteIcon();

        // Bounce animation
        btnFavorite.animate()
            .scaleX(1.3f).scaleY(1.3f).setDuration(150)
            .withEndAction(() ->
                btnFavorite.animate()
                    .scaleX(1f).scaleY(1f).setDuration(150).start()
            ).start();

        Toast.makeText(this,
            isFavorited ? "⭐ Added to Favorites!" : "Removed from Favorites",
            Toast.LENGTH_SHORT).show();
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorited
            ? android.R.drawable.btn_star_big_on
            : android.R.drawable.btn_star_big_off);
    }

    // ── Download ──────────────────────────────────────────────────────

    private void downloadWallpaper() {
        if (previewBitmap == null) {
            Toast.makeText(this, "Image still loading...", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Saving to gallery...", Toast.LENGTH_SHORT).show();
        WallpaperDownloader.saveToGallery(this, previewBitmap, wallpaper.getId(),
            new WallpaperDownloader.OnDownloadComplete() {
                @Override public void onSuccess(String path) {
                    runOnUiThread(() -> Toast.makeText(WallpaperDetailActivity.this,
                        "📸 Saved to Gallery!", Toast.LENGTH_SHORT).show());
                }
                @Override public void onError(String msg) {
                    runOnUiThread(() -> Toast.makeText(WallpaperDetailActivity.this,
                        "Download failed.", Toast.LENGTH_SHORT).show());
                }
            });
    }

    // ── Share ─────────────────────────────────────────────────────────

    private void shareWallpaper() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT,
            "Check out this amazing wallpaper!\n" +
            wallpaper.getAttributionText() +
            "\n\nFound on AuraWalls ✨ Download now!");
        startActivity(Intent.createChooser(share, "Share Wallpaper"));
    }

    // ── Image Loading ─────────────────────────────────────────────────

    private void loadImage() {
        Glide.with(this).asBitmap().load(wallpaper.getFullUrl())
            .into(new CustomTarget<Bitmap>() {
                @Override public void onResourceReady(Bitmap r, Transition<? super Bitmap> t) {
                    originalBitmap = r;
                    previewBitmap  = r;
                    ivPreview.setImageBitmap(r);
                    PaletteExtractor.extract(r, WallpaperDetailActivity.this::updatePalette);
                }
                @Override public void onLoadCleared(android.graphics.drawable.Drawable p) {}
            });
    }

    // ── Filters ───────────────────────────────────────────────────────

    private void applyFilters() {
        if (originalBitmap == null) return;
        float blur   = sbBlur.getProgress();
        float bright = (sbBrightness.getProgress() - 50) / 50f;
        float gray   = sbGrayscale.getProgress() / 100f;
        Bitmap bmp = originalBitmap;
        if (blur > 0)    bmp = ImageFilterHelper.applyBlur(this, bmp, blur);
        if (bright != 0) bmp = ImageFilterHelper.applyBrightness(bmp, bright);
        if (gray > 0)    bmp = ImageFilterHelper.applyGrayscale(bmp, gray);
        previewBitmap = bmp;
        ivPreview.setImageBitmap(bmp);
    }

    // ── Color Palette ─────────────────────────────────────────────────

    private void updatePalette(List<String> hexColors) {
        for (int i = 0; i < Math.min(5, hexColors.size()); i++) {
            String hex = hexColors.get(i);
            paletteViews[i].setBackgroundColor(Color.parseColor(hex));
            paletteHexTexts[i].setText(hex);
            final String h = hex;
            paletteViews[i].setOnClickListener(v -> {
                android.content.ClipboardManager cm =
                    (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(android.content.ClipData.newPlainText("hex", h));
                Toast.makeText(this, h + " copied!", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // ── Rewarded Ad ───────────────────────────────────────────────────

    private void loadRewardedAd() {
        RewardedAd.load(this, getString(R.string.admob_rewarded_id),
            new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
                @Override public void onAdLoaded(RewardedAd ad) { rewardedAd = ad; }
                @Override public void onAdFailedToLoad(LoadAdError e) { rewardedAd = null; }
            });
    }

    private void showRewardedAd() {
        if (rewardedAd != null) rewardedAd.show(this, reward -> unlockFilters());
        else unlockFilters();
    }

    private void unlockFilters() {
        filterPanel.setVisibility(View.VISIBLE);
        findViewById(R.id.btn_unlock_filters).setVisibility(View.GONE);
        Toast.makeText(this, "🎨 Filters unlocked!", Toast.LENGTH_SHORT).show();
    }
}
