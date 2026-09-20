package com.aurawalls.app.utils;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import java.io.IOException;

public class WallpaperSetter {
    private static final String TAG = "WallpaperSetter";
    public enum Target { HOME, LOCK, BOTH }

    public static boolean setWallpaper(Context context, Bitmap bitmap, Target target) {
        WallpaperManager wm = WallpaperManager.getInstance(context);
        try {
            DisplayMetrics dm = new DisplayMetrics();
            ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getRealMetrics(dm);
            int screenW = dm.widthPixels;
            int screenH = dm.heightPixels;

            Bitmap scaled = scaleBitmapToFillScreen(bitmap, screenW, screenH);
            wm.suggestDesiredDimensions(screenW, screenH);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                int flag;
                switch (target) {
                    case HOME: flag = WallpaperManager.FLAG_SYSTEM; break;
                    case LOCK: flag = WallpaperManager.FLAG_LOCK;   break;
                    default:   flag = WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK;
                }
                Rect visibleCrop = new Rect(0, 0, screenW, screenH);
                wm.setBitmap(scaled, visibleCrop, true, flag);
            } else {
                wm.setBitmap(scaled);
            }
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to set wallpaper: " + e.getMessage());
            return false;
        }
    }

    private static Bitmap scaleBitmapToFillScreen(Bitmap src, int screenW, int screenH) {
        float srcRatio    = (float) src.getWidth() / src.getHeight();
        float screenRatio = (float) screenW / screenH;

        int drawW, drawH, offsetX = 0, offsetY = 0;

        if (srcRatio > screenRatio) {
            // Image wider — fit height, center horizontally
            drawH = screenH;
            drawW = (int) (screenH * srcRatio);
            offsetX = (screenW - drawW) / 2;
        } else {
            // Image taller — fit width, center vertically
            drawW = screenW;
            drawH = (int) (screenW / srcRatio);
            offsetY = (screenH - drawH) / 2;
        }

        Bitmap result = Bitmap.createBitmap(screenW, screenH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(src, null,
            new Rect(offsetX, offsetY, offsetX + drawW, offsetY + drawH), paint);
        return result;
    }
}
