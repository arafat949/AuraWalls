package com.aurawalls.app.activities;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import com.aurawalls.app.utils.LocaleHelper;

/**
 * BaseActivity — all activities extend this so locale is always applied.
 */
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.wrap(newBase));
    }
}
