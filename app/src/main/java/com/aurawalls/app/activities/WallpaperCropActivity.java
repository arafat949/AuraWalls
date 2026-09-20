package com.aurawalls.app.activities;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.aurawalls.app.R;
import com.aurawalls.app.utils.WallpaperSetter;
import com.google.android.material.button.MaterialButton;
import java.io.IOException;

public class WallpaperCropActivity extends AppCompatActivity {

    public static final String EXTRA_TARGET = "target";
    private ImageView ivCropPreview;
    private Bitmap bitmap;
    private WallpaperSetter.Target target;

    private float offsetX = 0, offsetY = 0;
    private float lastTouchX, lastTouchY;
    private float scale;
    private int screenW, screenH;
    private int bitmapW, bitmapH;

    public static void start(Context ctx, Bitmap bmp, WallpaperSetter.Target target) {
        // Store bitmap in static cache
        BitmapCache.bitmap = bmp;
        Intent i = new Intent(ctx, WallpaperCropActivity.class);
        i.putExtra(EXTRA_TARGET, target.name());
        ctx.startActivity(i);
    }

    // Simple static cache for bitmap passing
    public static class BitmapCache {
        public static Bitmap bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallpaper_crop);

        bitmap = BitmapCache.bitmap;
        if (bitmap == null) { finish(); return; }

        target = WallpaperSetter.Target.valueOf(
            getIntent().getStringExtra(EXTRA_TARGET));

        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
            .getDefaultDisplay().getRealMetrics(dm);
        screenW = dm.widthPixels;
        screenH = dm.heightPixels;

        bitmapW = bitmap.getWidth();
        bitmapH = bitmap.getHeight();

        // Scale to fit width
        scale = (float) screenW / bitmapW;
        offsetX = 0;
        offsetY = (screenH - bitmapH * scale) / 2f;

        ivCropPreview = findViewById(R.id.iv_crop_preview);
        TextView tvHint = findViewById(R.id.tv_crop_hint);
        MaterialButton btnSet = findViewById(R.id.btn_crop_set);
        MaterialButton btnCancel = findViewById(R.id.btn_crop_cancel);

        tvHint.setText("Drag to position • Pinch to zoom");
        renderPreview();

        ivCropPreview.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getX() - lastTouchX;
                    float dy = event.getY() - lastTouchY;
                    offsetX += dx;
                    offsetY += dy;
                    // Clamp so image always covers screen
                    offsetX = Math.min(0, Math.max(offsetX, screenW - bitmapW * scale));
                    offsetY = Math.min(0, Math.max(offsetY, screenH - bitmapH * scale));
                    lastTouchX = event.getX();
                    lastTouchY = event.getY();
                    renderPreview();
                    break;
            }
            return true;
        });

        btnSet.setOnClickListener(v -> applyWallpaper());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void renderPreview() {
        Bitmap preview = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(preview);
        canvas.drawColor(Color.BLACK);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(offsetX, offsetY);
        canvas.drawBitmap(bitmap, matrix, paint);
        ivCropPreview.setImageBitmap(preview);
    }

    private void applyWallpaper() {
        // Create final cropped bitmap
        Bitmap result = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(offsetX, offsetY);
        canvas.drawBitmap(bitmap, matrix, paint);

        findViewById(R.id.btn_crop_set).setEnabled(false);
        new Thread(() -> {
            boolean ok = WallpaperSetter.setWallpaper(this, result, target);
            runOnUiThread(() -> {
                Toast.makeText(this,
                    ok ? "✅ Wallpaper set!" : "❌ Failed",
                    Toast.LENGTH_SHORT).show();
                if (ok) finish();
                else findViewById(R.id.btn_crop_set).setEnabled(true);
            });
        }).start();
    }
}
