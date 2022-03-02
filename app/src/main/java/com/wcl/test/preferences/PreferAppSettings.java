package com.wcl.test.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.wcl.test.base.BaseApp;

public class PreferAppSettings {
    private static String update_dialog_times = "update_dialog_times";//更新对话框
    private static final String KEY_LOG_TOGGLE = "KEY_LOG_TOGGLE";
    private static final String KEY_DEBUG_TOGGLE = "KEY_DEBUG_TOGGLE";

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
    }

    public static void clear() {
        getPreferences(BaseApp.getApp()).edit().clear().apply();
    }

    /**
     * 最后选择的相册ID
     */
    private static final String LAST_ALBUM_ID = "LAST_ALBUM_ID";

    public static void setLastAlbumId(String value) {
        SharedPreferences prefs = getPreferences(BaseApp.getApp());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_ALBUM_ID, value);
        editor.apply();
    }

    public static String getLastAlbumId() {
        return getPreferences(BaseApp.getApp()).getString(LAST_ALBUM_ID, "");
    }

    public static Long getUpdateDialogTimes() {
        return getPreferences(BaseApp.getApp()).getLong(update_dialog_times, 0);
    }

    public static void setUpdateDialogTimes(long value) {
        SharedPreferences prefs = getPreferences(BaseApp.getApp());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(update_dialog_times, value);
        editor.apply();
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
