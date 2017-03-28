package com.sasanka.msp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import com.sasanka.msp.common.MSPAlertHelper;
import com.sasanka.msp.common.MSPApplication;
import com.sasanka.msp.fragments.MSPLoginFragment;
import com.sasanka.msp.fragments.MSPPreLoginHomeFragment;
import com.sasanka.msp.fragments.MSPSignUpFragment;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;
import com.sasanka.msp.managers.MSPUserManagerProvider;
import com.sasanka.msp.R;

/**
 * This activity handles the pre-login flow.
 */
public class MSPPreLoginActivity extends AppCompatActivity
        implements MSPPreLoginHomeFragment.OnFragmentInteractionListener,
        MSPLoginFragment.OnFragmentInteractionListener,
        MSPSignUpFragment.OnFragmentInteractionListener {

    private static final String TAG_HOME    =   "com.sasanka.msp.home";
    private static final String TAG_LOGIN   =   "com.sasanka.msp.login";
    private static final String TAG_SIGN_UP =   "com.sasanka.msp.signup";

    private MSPUserInterface mUser = MSPUserManagerProvider.getProvider();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_login);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.container, MSPPreLoginHomeFragment.newInstance(), TAG_HOME)
                .commit();
        setTitle("Home");
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0)
            setTitle("Home");
    }

    @Override
    public void onLoginButtonClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MSPLoginFragment.newInstance(), TAG_LOGIN)
                .addToBackStack(null)
                .commit();
        setTitle("Login");
    }

    @Override
    public void onSignUpButtonClicked() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, MSPSignUpFragment.newInstance(), TAG_SIGN_UP)
                .addToBackStack(null)
                .commit();
        setTitle("Sign Up");
    }

    @Override
    public void onLoginButtonClicked(String email, String password, final String userType) {
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email id...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isFinishing())
            MSPAlertHelper.showProgressDialog(this, "Logging In...");
        mUser.login(email, password, userType, new MSPUserInterface.OnLoginListener() {
            @Override
            public void onLoginCompleted(Exception e) {
                if (!isFinishing())
                    MSPAlertHelper.dismissProgressDialog();
                if (e == null) {
                    if (mUser.getUserType().equals(userType)) {
                        Toast.makeText(MSPPreLoginActivity.this, "Successfully logged in...", Toast.LENGTH_SHORT)
                                .show();
                        startActivity(new Intent(MSPPreLoginActivity.this, MSPPostLoginHomeActivity.class));
                        finish();
                    } else {
                        mUser.logout();
                        Toast.makeText(MSPPreLoginActivity.this, "Please select the correct user type...",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MSPPreLoginActivity.this, "Login failed...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onSignUpButtonClicked(String fullName, String email, String password, String userType) {
        if (TextUtils.isEmpty(fullName)) {
            Toast.makeText(this, "Name cannot be empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email id...", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isFinishing())
            MSPAlertHelper.showProgressDialog(this, "Signing Up...");
        mUser.signUp(email, password, userType, fullName, new MSPUserInterface.OnSignUpListener() {
            @Override
            public void onSignUpCompleted(Exception e) {
                if (!isFinishing())
                    MSPAlertHelper.dismissProgressDialog();
                if (e == null) {
                    Toast.makeText(MSPPreLoginActivity.this, "Successfully Signed Up...", Toast.LENGTH_SHORT)
                            .show();
                    startActivity(new Intent(MSPPreLoginActivity.this, MSPPostLoginHomeActivity.class));
                    finish();
                } else {
                    Toast.makeText(MSPPreLoginActivity.this, "Sign Up failed...", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
