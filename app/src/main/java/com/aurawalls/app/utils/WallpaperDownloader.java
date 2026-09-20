package com.aurawalls.app.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class WallpaperDownloader {

    public interface OnDownloadComplete {
        void onSuccess(String path);
        void onError(String message);
    }

    /**
     * Saves a Bitmap to the device Gallery under "AuraWalls" album.
     * Must be called from a background thread.
     */
    public static void saveToGallery(Context ctx, Bitmap bitmap,
                                     String fileName, OnDownloadComplete cb) {
        try {
            String name = "AuraWalls_" + fileName + "_" + System.currentTimeMillis() + ".jpg";
            OutputStream out;
            String savedPath;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ — use MediaStore (no storage permission needed)
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, name);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/AuraWalls");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);

                Uri uri = ctx.getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) throw new Exception("MediaStore insert failed");

                out = ctx.getContentResolver().openOutputStream(uri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                if (out != null) out.close();

                values.clear();
                values.put(MediaStore.Images.Media.IS_PENDING, 0);
                ctx.getContentResolver().update(uri, values, null, null);
                savedPath = uri.toString();
            } else {
                // Android 9 and below
                File dir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), "AuraWalls");
                if (!dir.exists()) dir.mkdirs();
                File file = new File(dir, name);
                out = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out);
                out.close();
                savedPath = file.getAbsolutePath();

                // Notify gallery
                android.media.MediaScannerConnection.scanFile(ctx,
                        new String[]{savedPath}, null, null);
            }
            cb.onSuccess(savedPath);
        } catch (Exception e) {
            cb.onError(e.getMessage());
        }
    }
}
