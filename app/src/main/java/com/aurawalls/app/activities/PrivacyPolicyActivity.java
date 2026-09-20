package com.aurawalls.app.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.aurawalls.app.R;

/**
 * PrivacyPolicyActivity — Play Store mandatory.
 * WebView দিয়ে Privacy Policy দেখায়।
 * আপনার নিজের Privacy Policy URL দিন।
 */
public class PrivacyPolicyActivity extends BaseActivity {

    // ← আপনার Privacy Policy URL এখানে দিন
    private static final String PRIVACY_URL =
            "https://sites.google.com/view/aurawalls-privacy/home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        Toolbar tb = findViewById(R.id.toolbar);
        setSupportActionBar(tb);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Privacy Policy");
        }

        WebView wv = findViewById(R.id.webview);
        wv.setWebViewClient(new WebViewClient());
        wv.getSettings().setJavaScriptEnabled(false);
        wv.loadUrl(PRIVACY_URL);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { onBackPressed(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
