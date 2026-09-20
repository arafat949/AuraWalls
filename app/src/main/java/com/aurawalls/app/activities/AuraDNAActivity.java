package com.aurawalls.app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.aurawalls.app.R;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Random;

public class AuraDNAActivity extends AppCompatActivity {

    private ImageView ivAuraCard;
    private TextView  tvAuraTitle, tvAuraType, tvAuraScore;
    private MaterialButton btnShare, btnRegenerate, btnSave;
    private Bitmap cardBitmap;
    private String auraTitle, auraType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aura_dna);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("✨ Your Aura DNA");
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        auraTitle = prefs.getString("aura_title", "Cosmic Explorer");
        auraType  = prefs.getString("aura_type",  "COSMIC_EXPLORER");

        bindViews();
        generateCard();
        setupButtons();
    }

    private void bindViews() {
        ivAuraCard     = findViewById(R.id.iv_aura_card);
        tvAuraTitle    = findViewById(R.id.tv_aura_dna_title);
        tvAuraType     = findViewById(R.id.tv_aura_dna_type);
        tvAuraScore    = findViewById(R.id.tv_aura_dna_score);
        btnShare       = findViewById(R.id.btn_share_dna);
        btnRegenerate  = findViewById(R.id.btn_regenerate);
        btnSave        = findViewById(R.id.btn_save_card);

        tvAuraTitle.setText(auraTitle);
        tvAuraType.setText(getEmojiForType(auraType) + "  " + auraType.replace("_", " "));
    }

    private void generateCard() {
        int[] colors = getColorsForType(auraType);
        cardBitmap = drawAuraDNACard(auraTitle, auraType, colors);
        ivAuraCard.setImageBitmap(cardBitmap);

        // Aura score
        int score = 70 + new Random().nextInt(30);
        tvAuraScore.setText("Aura Score: " + score + "/100");
    }

    // ── Card Drawing ──────────────────────────────────────────────────

    private Bitmap drawAuraDNACard(String title, String type, int[] colors) {
        int W = 800, H = 1000;
        Bitmap bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        // Background gradient
        Paint bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient bgGrad = new LinearGradient(0, 0, W, H,
            colors[0], colors[1], Shader.TileMode.CLAMP);
        bgPaint.setShader(bgGrad);
        canvas.drawRoundRect(new RectF(0, 0, W, H), 40, 40, bgPaint);

        // Dark overlay for readability
        Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        overlayPaint.setColor(Color.parseColor("#66000000"));
        canvas.drawRoundRect(new RectF(0, 0, W, H), 40, 40, overlayPaint);

        // Hexagonal DNA pattern
        drawHexPattern(canvas, W, H, colors[2]);

        // AuraWalls watermark
        Paint watermarkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        watermarkPaint.setColor(Color.parseColor("#AAFFFFFF"));
        watermarkPaint.setTextSize(28f);
        watermarkPaint.setTypeface(Typeface.DEFAULT_BOLD);
        canvas.drawText("✨ AuraWalls", 40, 60, watermarkPaint);

        // Emoji large
        String emoji = getEmojiForType(type);
        Paint emojiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        emojiPaint.setTextSize(120f);
        emojiPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(emoji, W / 2f, 320, emojiPaint);

        // "Your Aura is"
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.parseColor("#CCFFFFFF"));
        labelPaint.setTextSize(32f);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Your Aura is", W / 2f, 420, labelPaint);

        // Aura Title
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.WHITE);
        titlePaint.setTextSize(64f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(title, W / 2f, 510, titlePaint);

        // Divider line
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(Color.parseColor("#66FFFFFF"));
        linePaint.setStrokeWidth(2f);
        canvas.drawLine(W / 2f - 100, 540, W / 2f + 100, 540, linePaint);

        // DNA bars (unique pattern)
        drawDNABars(canvas, W, H, colors[2]);

        // Bottom stats
        drawStats(canvas, W, H, type);

        // Bottom watermark
        Paint bottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bottomPaint.setColor(Color.parseColor("#88FFFFFF"));
        bottomPaint.setTextSize(22f);
        bottomPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("aurawalls.app  •  Discover Your Aura", W / 2f, H - 40, bottomPaint);

        return bmp;
    }

    private void drawHexPattern(Canvas canvas, int W, int H, int accentColor) {
        Paint hexPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hexPaint.setColor(accentColor);
        hexPaint.setAlpha(30);
        hexPaint.setStyle(Paint.Style.STROKE);
        hexPaint.setStrokeWidth(2f);

        int size = 60;
        for (int row = 0; row < H / size + 2; row++) {
            for (int col = 0; col < W / size + 2; col++) {
                float cx = col * size * 1.5f;
                float cy = row * size * 1.73f + (col % 2 == 0 ? 0 : size * 0.87f);
                drawHexagon(canvas, cx, cy, size * 0.5f, hexPaint);
            }
        }
    }

    private void drawHexagon(Canvas canvas, float cx, float cy, float r, Paint paint) {
        Path path = new Path();
        for (int i = 0; i < 6; i++) {
            double angle = Math.PI / 3 * i - Math.PI / 6;
            float x = cx + r * (float) Math.cos(angle);
            float y = cy + r * (float) Math.sin(angle);
            if (i == 0) path.moveTo(x, y);
            else path.lineTo(x, y);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawDNABars(Canvas canvas, int W, int H, int accentColor) {
        Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setAlpha(180);

        Random rnd = new Random(auraType.hashCode());
        int barCount = 12;
        float barWidth = (W - 120f) / barCount;
        float maxBarH = 100f;
        float baseY = 680f;

        for (int i = 0; i < barCount; i++) {
            float barH = 20f + rnd.nextFloat() * maxBarH;
            float x = 60 + i * barWidth;

            // Gradient bar
            LinearGradient grad = new LinearGradient(
                x, baseY - barH, x, baseY,
                accentColor, Color.parseColor("#22FFFFFF"),
                Shader.TileMode.CLAMP);
            barPaint.setShader(grad);

            RectF rect = new RectF(x + 4, baseY - barH, x + barWidth - 4, baseY);
            canvas.drawRoundRect(rect, 8, 8, barPaint);
        }
    }

    private void drawStats(Canvas canvas, int W, int H, String type) {
        String[] statLabels = {"Energy", "Depth", "Creativity", "Harmony"};
        int[] statValues = getStatsForType(type);

        Paint labelP = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelP.setColor(Color.parseColor("#AAFFFFFF"));
        labelP.setTextSize(22f);
        labelP.setTextAlign(Paint.Align.CENTER);

        Paint barBg = new Paint(Paint.ANTI_ALIAS_FLAG);
        barBg.setColor(Color.parseColor("#33FFFFFF"));

        Paint barFg = new Paint(Paint.ANTI_ALIAS_FLAG);
        barFg.setColor(Color.parseColor("#AAFFFFFF"));

        float startY = 720f;
        float colW = W / 4f;

        for (int i = 0; i < 4; i++) {
            float cx = colW * i + colW / 2f;

            // Label
            canvas.drawText(statLabels[i], cx, startY, labelP);

            // Bar background
            canvas.drawRoundRect(
                new RectF(cx - 30, startY + 10, cx + 30, startY + 16),
                3, 3, barBg);

            // Bar fill
            float fillW = 60f * statValues[i] / 100f;
            canvas.drawRoundRect(
                new RectF(cx - 30, startY + 10, cx - 30 + fillW, startY + 16),
                3, 3, barFg);

            // Value
            Paint valP = new Paint(labelP);
            valP.setColor(Color.WHITE);
            valP.setTextSize(18f);
            canvas.drawText(statValues[i] + "%", cx, startY + 36, valP);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private int[] getColorsForType(String type) {
        switch (type) {
            case "MIDNIGHT_DREAMER": return new int[]{Color.parseColor("#1A1A2E"), Color.parseColor("#16213E"), Color.parseColor("#6C63FF")};
            case "GOLDEN_HOUR_SOUL": return new int[]{Color.parseColor("#F4A460"), Color.parseColor("#D2691E"), Color.parseColor("#FFD700")};
            case "NEON_WANDERER":    return new int[]{Color.parseColor("#FF006E"), Color.parseColor("#8338EC"), Color.parseColor("#FB5607")};
            case "ZEN_MASTER":       return new int[]{Color.parseColor("#2D6A4F"), Color.parseColor("#40916C"), Color.parseColor("#95D5B2")};
            case "COSMIC_EXPLORER":  return new int[]{Color.parseColor("#03045E"), Color.parseColor("#0077B6"), Color.parseColor("#90E0EF")};
            case "WILD_HEART":       return new int[]{Color.parseColor("#1B4332"), Color.parseColor("#2D6A4F"), Color.parseColor("#74C69D")};
            case "URBAN_LEGEND":     return new int[]{Color.parseColor("#212529"), Color.parseColor("#343A40"), Color.parseColor("#6C757D")};
            default:                 return new int[]{Color.parseColor("#FFB5C8"), Color.parseColor("#FF85A1"), Color.parseColor("#FF6B9D")};
        }
    }

    private String getEmojiForType(String type) {
        switch (type) {
            case "MIDNIGHT_DREAMER": return "🌙";
            case "GOLDEN_HOUR_SOUL": return "🌅";
            case "NEON_WANDERER":    return "⚡";
            case "ZEN_MASTER":       return "🍃";
            case "COSMIC_EXPLORER":  return "🚀";
            case "WILD_HEART":       return "🌿";
            case "URBAN_LEGEND":     return "🏙️";
            default:                 return "🌸";
        }
    }

    private int[] getStatsForType(String type) {
        Random rnd = new Random(type.hashCode());
        return new int[]{
            60 + rnd.nextInt(40),
            60 + rnd.nextInt(40),
            60 + rnd.nextInt(40),
            60 + rnd.nextInt(40)
        };
    }

    // ── Buttons ───────────────────────────────────────────────────────

    private void setupButtons() {
        btnShare.setOnClickListener(v -> shareCard());
        btnSave.setOnClickListener(v -> saveCard());
        btnRegenerate.setOnClickListener(v -> {
            generateCard();
            Toast.makeText(this, "New Aura DNA generated! ✨", Toast.LENGTH_SHORT).show();
        });
    }

    private void shareCard() {
        try {
            File cachePath = new File(getCacheDir(), "aura_dna.png");
            FileOutputStream fos = new FileOutputStream(cachePath);
            cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            Uri imageUri = FileProvider.getUriForFile(this,
                getPackageName() + ".provider", cachePath);

            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("image/png");
            share.putExtra(Intent.EXTRA_STREAM, imageUri);
            share.putExtra(Intent.EXTRA_TEXT,
                "My Aura is " + auraTitle + "! ✨\nDiscover yours on AuraWalls 🌟\n#AuraWalls #MyAura");
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(share, "Share Your Aura DNA"));
        } catch (Exception e) {
            Toast.makeText(this, "Share failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveCard() {
        try {
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "AuraWalls");
            if (!dir.exists()) dir.mkdirs();

            File file = new File(dir, "AuraDNA_" + System.currentTimeMillis() + ".png");
            FileOutputStream fos = new FileOutputStream(file);
            cardBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            // Notify gallery
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));

            Toast.makeText(this, "Saved to Gallery! 📸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
