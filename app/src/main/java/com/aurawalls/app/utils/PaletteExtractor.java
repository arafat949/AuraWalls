package com.aurawalls.app.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

public class PaletteExtractor {

    public interface OnPaletteReady {
        void onPaletteReady(List<String> hexColors);
    }

    /**
     * Extracts up to 5 dominant hex colours from a bitmap asynchronously.
     */
    public static void extract(Bitmap bitmap, OnPaletteReady callback) {
        Palette.from(bitmap).maximumColorCount(5).generate(palette -> {
            List<String> hexList = new ArrayList<>();
            if (palette != null) {
                // Try each swatch type; fall back to default grey
                int[] swatches = {
                        palette.getDominantColor(Color.GRAY),
                        palette.getVibrantColor(Color.GRAY),
                        palette.getMutedColor(Color.GRAY),
                        palette.getDarkVibrantColor(Color.GRAY),
                        palette.getLightVibrantColor(Color.GRAY)
                };
                for (int color : swatches) {
                    hexList.add(String.format("#%06X", (0xFFFFFF & color)));
                }
            }
            callback.onPaletteReady(hexList);
        });
    }

    public static String toHex(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}
