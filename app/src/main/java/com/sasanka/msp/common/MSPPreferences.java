package com.sasanka.msp.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Class which handles storing values to and retrieving values from SharedPreferences.
 */
public class MSPPreferences {

	public static final String PREF_KEY = "com.sasanka.msp";

	public static SharedPreferences getPreferences() {
		return MSPApplication.getContext().getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE);
	}

	public static Editor getPreferencesEditor() {
		return MSPPreferences.getPreferences().edit();
	}

	public static boolean contains(final String key) {
		return getPreferences().contains(key);
	}

	public static void putBoolean(String key, boolean value) {
		getPreferencesEditor().putBoolean(key, value).apply();
	}

	public static void putString(String key, String value) {
		getPreferencesEditor().putString(key, value).apply();
	}

	public static boolean getBoolean(String key) {
		return getPreferences().getBoolean(key, false);
	}

	public static String getString(String key) {
		return getPreferences().getString(key, "");
	}

	public static void remove(final String key) {
        getPreferencesEditor().remove(key).apply();
    }
}
