package com.aurawalls.app.activities;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import androidx.preference.PreferenceManager;

import com.aurawalls.app.R;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DURATION = 2200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        animateSplash();

        new Handler().postDelayed(() -> {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            Intent intent = prefs.getBoolean("onboarding_done", false)
                    ? new Intent(this, MainActivity.class)
                    : new Intent(this, OnboardingActivity.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }

    private void animateSplash() {
        View emoji    = findViewById(R.id.tv_splash_emoji);
        View title    = findViewById(R.id.tv_splash_title);
        View tagline  = findViewById(R.id.tv_splash_tagline);
        View version  = findViewById(R.id.tv_splash_version);
        View shimmer  = findViewById(R.id.splash_shimmer);

        // Start invisible
        emoji.setAlpha(0f);   emoji.setScaleX(0.5f); emoji.setScaleY(0.5f);
        title.setAlpha(0f);   title.setTranslationY(30f);
        tagline.setAlpha(0f); tagline.setTranslationY(20f);
        version.setAlpha(0f);

        // Emoji pop
        AnimatorSet emojiAnim = new AnimatorSet();
        emojiAnim.playTogether(
            ObjectAnimator.ofFloat(emoji, "alpha", 0f, 1f).setDuration(400),
            ObjectAnimator.ofFloat(emoji, "scaleX", 0.5f, 1.1f, 1f).setDuration(500),
            ObjectAnimator.ofFloat(emoji, "scaleY", 0.5f, 1.1f, 1f).setDuration(500)
        );
        emojiAnim.setInterpolator(new DecelerateInterpolator());
        emojiAnim.setStartDelay(200);
        emojiAnim.start();

        // Title slide up
        AnimatorSet titleAnim = new AnimatorSet();
        titleAnim.playTogether(
            ObjectAnimator.ofFloat(title, "alpha", 0f, 1f).setDuration(400),
            ObjectAnimator.ofFloat(title, "translationY", 30f, 0f).setDuration(400)
        );
        titleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        titleAnim.setStartDelay(550);
        titleAnim.start();

        // Tagline slide up
        AnimatorSet taglineAnim = new AnimatorSet();
        taglineAnim.playTogether(
            ObjectAnimator.ofFloat(tagline, "alpha", 0f, 1f).setDuration(400),
            ObjectAnimator.ofFloat(tagline, "translationY", 20f, 0f).setDuration(400)
        );
        taglineAnim.setStartDelay(750);
        taglineAnim.start();

        // Version fade
        ObjectAnimator versionAnim = ObjectAnimator.ofFloat(version, "alpha", 0f, 0.6f);
        versionAnim.setDuration(400);
        versionAnim.setStartDelay(1000);
        versionAnim.start();

        // Shimmer pulse on shimmer bar
        if (shimmer != null) {
            ObjectAnimator shimmerAnim = ObjectAnimator.ofFloat(shimmer, "alpha", 0f, 0.7f, 0f);
            shimmerAnim.setDuration(1200);
            shimmerAnim.setStartDelay(1000);
            shimmerAnim.start();
        }
    }
}
