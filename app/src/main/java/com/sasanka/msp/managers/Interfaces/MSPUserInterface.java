package com.sasanka.msp.managers.Interfaces;

import android.content.Context;
import android.location.Location;

/**
 * This interface contains all the operations related to a user.
 */
public interface MSPUserInterface {

    /**
     * listener for logging in a user.
     */
    interface OnLoginListener {
        /**
         * Called when login operation is completed.
         * @param e not null in case of failure else null.
         */
        void onLoginCompleted(Exception e);
    }

    /**
     * listener for signing up a user.
     */
    interface OnSignUpListener {
        /**
         * Called when signUp operation is completed.
         * @param e not null in case of failure else null.
         */
        void onSignUpCompleted(Exception e);
    }

    /**
     * Used to check if there exists a logged in user.
     * @return boolean.
     */
    boolean isAuthenticated();

    /**
     * Returns the unique id associated with a user.
     * @return String.
     */
    String getObjectId();

    /**
     * Returns the username of the user.
     * @return String.
     */
    String getUsername();

    /**
     * Returns the full name of user.
     * @return String.
     */
    String getFullName();

    /**
     * Return the userType of user. Either "Vendor" or "User".
     * @return String.
     */
    String getUserType();

    /**
     * Checks if the user is a vendor.
     * @return boolean.
     */
    boolean isVendor();

    /**
     * Used to update user's new location to server.
     * @param location user's current location.
     */
    void updateUserLocation(Location location);

    /**
     * This method is used to login a user.
     * @param username username of user.
     * @param password password of user.
     * @param userType userType of user.
     * @param listener OnLoginListener.
     */
    void login(String username, String password, String userType, OnLoginListener listener);

    /**
     * This method is used to signUp a user.
     * @param username username of user.
     * @param password password of user.
     * @param userType userType of user.
     * @param fullName fullName of user.
     * @param listener OnSignUpListener.
     */
    void signUp(String username, String password, String userType, String fullName, OnSignUpListener listener);

    /**
     * This method logs out the existing user and performs the actions
     * that are to be done before logging out a user.
     * @param context Context.
     */
    void logout(Context context);

    /**
     * This method logs out the existing user.
     */
    void logout();
}
