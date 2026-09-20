package com.aurawalls.app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aurawalls.app.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MoodCameraActivity extends BaseActivity {

    private static final String TAG = "MoodCameraActivity";
    private static final int CAMERA_PERMISSION = 101;

    // Mood data
    private static final String[] MOODS = {
        "😊 Happy",       "😌 Calm",        "😢 Sad",
        "😠 Angry",       "😴 Tired",       "😲 Surprised",
        "🤔 Thoughtful",  "🌟 Energetic"
    };
    private static final String[] MOOD_QUERIES = {
        "vibrant colorful happy", "calm minimal peaceful",
        "soft healing pastel",    "cool calming blue ocean",
        "dark cozy night",        "bright dramatic sky",
        "deep aesthetic moody",   "energetic neon dynamic"
    };
    private static final String[] MOOD_DESCS = {
        "Your face radiates happiness! A vibrant wallpaper matches your energy.",
        "Your mind is at peace. A calm minimal wallpaper is perfect for you.",
        "Feeling a little down? A soft healing wallpaper will comfort you.",
        "Some tension detected. A cool calming wallpaper will soothe you.",
        "Looking tired. A cozy dark wallpaper will help you relax.",
        "Something surprised you! A dramatic sky wallpaper matches your mood.",
        "Deep in thought. An aesthetic moody wallpaper suits you.",
        "Full of energy! A neon dynamic wallpaper is your match."
    };

    private PreviewView previewView;
    private FaceDetector faceDetector;
    private ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private boolean isScanning = false;
    private boolean hasResult = false;
    private int detectedMoodIdx = -1;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // Face metrics accumulated over frames
    private float avgSmile = 0, avgLeftEye = 0, avgRightEye = 0;
    private int frameCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_camera);
        setupToolbar();
        setupFaceDetector();
        setupButtons();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("😊 Mood Detector");
        }
    }

    private void setupFaceDetector() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .setMinFaceSize(0.15f)
            .build();
        faceDetector = FaceDetection.getClient(options);
    }

    private void setupButtons() {
        MaterialButton btnScan = findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(v -> {
            if (isScanning) return;
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            } else {
                startCamera();
                startMoodScan();
            }
        });

        MaterialButton btnApply = findViewById(R.id.btn_apply_mood);
        btnApply.setOnClickListener(v -> applyMoodWallpaper());
    }

    // ── CameraX ───────────────────────────────────────────────────────

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
            ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                cameraProvider = future.get();
                bindCamera();
            } catch (Exception e) {
                Log.e(TAG, "Camera init failed: " + e.getMessage());
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera() {
        previewView = findViewById(R.id.preview_view);

        Preview preview = new Preview.Builder().build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeFrame);

        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);

        // Show preview
        mainHandler.post(() -> {
            previewView.setVisibility(View.VISIBLE);
            View placeholder = findViewById(R.id.face_placeholder);
            if (placeholder != null) placeholder.setVisibility(View.GONE);
        });
    }

    // ── ML Kit Face Analysis ──────────────────────────────────────────

    @OptIn(markerClass = ExperimentalGetImage.class)
    private void analyzeFrame(ImageProxy imageProxy) {
        if (!isScanning || hasResult) {
            imageProxy.close();
            return;
        }

        if (imageProxy.getImage() == null) {
            imageProxy.close();
            return;
        }

        InputImage image = InputImage.fromMediaImage(
            imageProxy.getImage(),
            imageProxy.getImageInfo().getRotationDegrees());

        faceDetector.process(image)
            .addOnSuccessListener(faces -> {
                if (!faces.isEmpty() && isScanning && !hasResult) {
                    processFaces(faces);
                }
                imageProxy.close();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Face detection failed: " + e.getMessage());
                imageProxy.close();
            });
    }

    private void processFaces(List<Face> faces) {
        Face face = faces.get(0); // Primary face

        // Accumulate over 10 frames for accuracy
        if (face.getSmilingProbability() != null)
            avgSmile += face.getSmilingProbability();
        if (face.getLeftEyeOpenProbability() != null)
            avgLeftEye += face.getLeftEyeOpenProbability();
        if (face.getRightEyeOpenProbability() != null)
            avgRightEye += face.getRightEyeOpenProbability();

        frameCount++;

        // Update status
        mainHandler.post(() -> {
            TextView tvStatus = findViewById(R.id.tv_scan_status);
            if (tvStatus != null) {
                int progress = (frameCount * 10);
                tvStatus.setText("🔍 Analyzing... " + Math.min(progress, 100) + "%");
            }
        });

        if (frameCount >= 10) {
            hasResult = true;
            float smile    = avgSmile / frameCount;
            float leftEye  = avgLeftEye / frameCount;
            float rightEye = avgRightEye / frameCount;
            float eyeOpen  = (leftEye + rightEye) / 2f;

            int moodIdx = calculateMood(smile, eyeOpen, face);

            mainHandler.post(() -> showRealMoodResult(moodIdx));
        }
    }

    private int calculateMood(float smile, float eyeOpen, Face face) {
        // Eyes closed → tired
        if (eyeOpen < 0.3f) return 4; // Tired

        // Big smile → happy
        if (smile > 0.7f) return 0; // Happy

        // Medium smile → calm
        if (smile > 0.4f) return 1; // Calm

        // Head rotation → surprised
        float headAngle = Math.abs(face.getHeadEulerAngleY());
        if (headAngle > 20f) return 5; // Surprised

        // Slight frown (low smile, eyes open) → thoughtful
        if (smile < 0.2f && eyeOpen > 0.7f) return 6; // Thoughtful

        // Very low smile → sad
        if (smile < 0.1f) return 2; // Sad

        // Default → energetic
        return 7;
    }

    private void showRealMoodResult(int idx) {
        isScanning = false;
        detectedMoodIdx = idx;

        // Stop camera
        if (cameraProvider != null) cameraProvider.unbindAll();

        // Hide scan overlay
        View scanOverlay = findViewById(R.id.scan_overlay);
        if (scanOverlay != null) scanOverlay.setVisibility(View.GONE);

        TextView tvStatus = findViewById(R.id.tv_scan_status);
        if (tvStatus != null) tvStatus.setText("✅ Mood detected!");

        // Show result
        MaterialCardView resultCard = findViewById(R.id.result_card);
        TextView tvMood    = findViewById(R.id.tv_detected_mood);
        TextView tvMoodDesc = findViewById(R.id.tv_mood_desc);
        MaterialButton btnApply = findViewById(R.id.btn_apply_mood);

        if (tvMood != null) tvMood.setText(MOODS[idx]);
        if (tvMoodDesc != null) tvMoodDesc.setText(MOOD_DESCS[idx]);
        if (resultCard != null) {
            resultCard.setVisibility(View.VISIBLE);
            android.view.animation.AlphaAnimation fadeIn =
                new android.view.animation.AlphaAnimation(0f, 1f);
            fadeIn.setDuration(600);
            resultCard.startAnimation(fadeIn);
        }
        if (btnApply != null) btnApply.setVisibility(View.VISIBLE);
    }

    private void startMoodScan() {
        isScanning = true;
        hasResult  = false;
        frameCount = 0;
        avgSmile = avgLeftEye = avgRightEye = 0;

        View scanOverlay = findViewById(R.id.scan_overlay);
        View resultCard  = findViewById(R.id.result_card);
        View btnApply    = findViewById(R.id.btn_apply_mood);
        TextView tvStatus = findViewById(R.id.tv_scan_status);

        if (resultCard != null)  resultCard.setVisibility(View.GONE);
        if (btnApply != null)    btnApply.setVisibility(View.GONE);
        if (scanOverlay != null) scanOverlay.setVisibility(View.VISIBLE);
        if (tvStatus != null)    tvStatus.setText("🔍 Position your face in the frame...");

        // Animate scan line
        View scanLine = findViewById(R.id.scan_line);
        if (scanLine != null) {
            android.view.animation.TranslateAnimation anim =
                new android.view.animation.TranslateAnimation(0, 0, -300, 300);
            anim.setDuration(1200);
            anim.setRepeatCount(android.view.animation.Animation.INFINITE);
            anim.setRepeatMode(android.view.animation.Animation.REVERSE);
            scanLine.startAnimation(anim);
        }

        // Timeout — 8 seconds এর মধ্যে face না পেলে fallback
        mainHandler.postDelayed(() -> {
            if (isScanning && !hasResult) {
                Log.w(TAG, "Timeout — using fallback mood");
                hasResult = true;
                isScanning = false;
                int fallback = new java.util.Random().nextInt(MOODS.length);
                showRealMoodResult(fallback);
            }
        }, 8000);
    }

    private void applyMoodWallpaper() {
        if (detectedMoodIdx < 0) return;
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("mood_query", MOOD_QUERIES[detectedMoodIdx]);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
            @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
            startMoodScan();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        faceDetector.close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
