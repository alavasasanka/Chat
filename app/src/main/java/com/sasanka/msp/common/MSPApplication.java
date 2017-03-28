package com.sasanka.msp.common;

import android.app.Application;
import android.content.Context;

import com.sasanka.msp.managers.MSPServerManagerProvider;

public class MSPApplication extends Application {

    private static MSPApplication sInstance;
    private static Context sContext;
    private static boolean isInBackground = true;

    public static synchronized MSPApplication getSharedInstance() {
        if(sInstance == null)
            sInstance = new MSPApplication();
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        MSPServerManagerProvider.getProvider();
    }

    /**
     * To notify that the app is in foreground.
     */
    public static void onResumed() {
        isInBackground = false;
    }

    /**
     * To notify that the app is in background.
     */
    public static void onPaused() {
        isInBackground = true;
    }

    /**
     * To know if the app is in background.
     * @return boolean
     */
    public static boolean isInBackground() {
        return isInBackground;
    }

    public static Context getContext() {
        return sContext;
    }
}
