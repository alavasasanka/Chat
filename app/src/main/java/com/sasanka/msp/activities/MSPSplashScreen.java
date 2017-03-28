package com.sasanka.msp.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.R;

/**
 * This activity redirects either to pre-login flow or post-login flow depending upon the user.
 */
public class MSPSplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
                actionBar.hide();
        }
        setContentView(R.layout.activity_splash_screen);
        if (!MSPUserManagerProvider.getProvider().isAuthenticated()) {
            startActivity(new Intent(this, MSPPreLoginActivity.class));
            finish();
        } else {
            Intent intent = new Intent(this, MSPPostLoginHomeActivity.class);
            if (getIntent() != null && getIntent().hasExtra(MSPConstants.PushNotification.payload))
                intent.putExtras(getIntent());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MSPApplication.onResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MSPApplication.onPaused();
    }
}
