package com.sasanka.msp.managers;

import android.content.Context;
import android.location.Location;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.sasanka.msp.common.MSPConstants;
import com.sasanka.msp.database.MSPChatDBHelper;
import com.sasanka.msp.managers.Interfaces.MSPUserInterface;

import org.json.JSONObject;

/**
 * This class gives the implementations for user actions.
 */
public class MSPUserManager implements MSPUserInterface {

    private static MSPUserManager sInstance;

    private MSPUserManager() {}

    protected static MSPUserManager getSharedInstance() {
        if (sInstance == null) {
            sInstance = new MSPUserManager();
        }
        return sInstance;
    }

    protected Object get(String key) {
        if ((getCurrentUser() != null) && contains(key))
            return getCurrentUser().get(key);
        else
            return null;
    }

    protected boolean contains(String key) {
        return getCurrentUser().containsKey(key);
    }

    protected String getString(String key) {
        Object obj = this.get(key);
        if ((obj != null) && (obj != JSONObject.NULL) && (obj instanceof String)) {
            return obj.toString();
        }
        else
            return "";
    }

    protected ParseUser getCurrentUser() {
        return ParseUser.getCurrentUser();
    }

    @Override
    public boolean isAuthenticated() {
        if (getCurrentUser() != null && getCurrentUser().getObjectId() != null)
            return getCurrentUser().isAuthenticated();
        else
            return false;
    }

    @Override
    public String getObjectId() {
        return getCurrentUser().getObjectId();
    }

    @Override
    public String getUsername() {
        return getCurrentUser().getUsername();
    }

    @Override
    public String getFullName() {
        return getString(MSPConstants.ServerKeys.fullName);
    }

    @Override
    public String getUserType() {
        return getString(MSPConstants.ServerKeys.userType);
    }

    @Override
    public boolean isVendor() {
        return getUserType().equals(MSPConstants.ServerKeys.vendor);
    }

    @Override
    public void updateUserLocation(Location location) {
        getCurrentUser().put(MSPConstants.ServerKeys.location,
                new ParseGeoPoint(location.getLatitude(), location.getLongitude()));
        getCurrentUser().saveInBackground();
    }

    @Override
    public void login(String username, String password, final String userType, final OnLoginListener listener) {
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    listener.onLoginCompleted(null);
                } else {
                    listener.onLoginCompleted(e);
                }
            }
        });
    }

    @Override
    public void signUp(String username, String password, String userType,
                       String fullName, final OnSignUpListener listener) {
        ParseUser newUser = new ParseUser();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.put(MSPConstants.ServerKeys.userType, userType);
        newUser.put(MSPConstants.ServerKeys.fullName, fullName);
        newUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null)
                    listener.onSignUpCompleted(null);
                else
                    listener.onSignUpCompleted(e);
            }
        });
    }

    @Override
    public void logout(Context context) {
        MSPChatDBHelper.getSharedInstance(context).reset();
        ParseInstallation currentInstallation = ParseInstallation.getCurrentInstallation();
        currentInstallation.remove(MSPConstants.ServerKeys.userObjectId);
        currentInstallation.saveInBackground();
        ParseUser.logOut();
    }

    @Override
    public void logout() {
        ParseUser.logOut();
    }
}
