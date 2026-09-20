package com.aurawalls.app.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.content.Context;

public class ImageFilterHelper {

    /**
     * Apply Gaussian blur using RenderScript.
     * @param radius  1f–25f
     */
    public static Bitmap applyBlur(Context context, Bitmap source, float radius) {
        if (radius <= 0) return source;
        radius = Math.min(radius, 25f);

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), source.getConfig());
        RenderScript rs   = RenderScript.create(context);
        Allocation   in   = Allocation.createFromBitmap(rs, source);
        Allocation   out  = Allocation.createFromBitmap(rs, output);
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setRadius(radius);
        blur.setInput(in);
        blur.forEach(out);
        out.copyTo(output);
        rs.destroy();
        return output;
    }

    /**
     * Adjust brightness. factor: -1f (black) → 0f (original) → 1f (white).
     */
    public static Bitmap applyBrightness(Bitmap source, float factor) {
        int shift = (int)(factor * 255);
        ColorMatrix cm = new ColorMatrix(new float[]{
                1, 0, 0, 0, shift,
                0, 1, 0, 0, shift,
                0, 0, 1, 0, shift,
                0, 0, 0, 1, 0
        });
        return applyColorMatrix(source, cm);
    }

    /**
     * Apply grayscale. amount: 0f = full colour, 1f = full grey.
     */
    public static Bitmap applyGrayscale(Bitmap source, float amount) {
        float inv = 1f - amount;
        float r = 0.2126f, g = 0.7152f, b = 0.0722f;
        ColorMatrix cm = new ColorMatrix(new float[]{
                inv + amount * r, amount * g,       amount * b,       0, 0,
                amount * r,       inv + amount * g, amount * b,       0, 0,
                amount * r,       amount * g,       inv + amount * b, 0, 0,
                0,                0,                0,                1, 0
        });
        return applyColorMatrix(source, cm);
    }

    private static Bitmap applyColorMatrix(Bitmap source, ColorMatrix cm) {
        Bitmap output = source.copy(source.getConfig(), true);
        Canvas canvas = new Canvas(output);
        Paint  paint  = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(source, 0, 0, paint);
        return output;
    }
}
