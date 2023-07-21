package com.wcl.test.storage;

import android.content.Context;
import android.content.SharedPreferences;

import com.wcl.test.base.BaseApp;

public class ToggleSettings {
    private static final String KEY_LOG_TOGGLE = "KEY_LOG_TOGGLE";
    private static final String KEY_DEBUG_TOGGLE = "KEY_DEBUG_TOGGLE";

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences("app_toggle_settings", Context.MODE_PRIVATE);
    }

    public static void clear() {
        getPreferences(BaseApp.getApp()).edit().clear().apply();
    }

    public static void setLogEnable(boolean value) {
        SharedPreferences prefs = getPreferences(BaseApp.getApp());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_LOG_TOGGLE, value);
        editor.apply();
    }

    public static boolean getLogEnable() {
        return getPreferences(BaseApp.getApp()).getBoolean(KEY_LOG_TOGGLE, false);
    }

    public static void setDebugEnable(boolean value) {
        SharedPreferences prefs = getPreferences(BaseApp.getApp());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_DEBUG_TOGGLE, value);
        editor.apply();
    }

    public static boolean getDebugEnable() {
        return getPreferences(BaseApp.getApp()).getBoolean(KEY_DEBUG_TOGGLE, false);
    }
}
