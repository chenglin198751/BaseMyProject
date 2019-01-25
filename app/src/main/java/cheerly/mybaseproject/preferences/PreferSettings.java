package cheerly.mybaseproject.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import cheerly.mybaseproject.base.BaseApp;

public class PreferSettings {
    private static String update_dialog_times = "update_dialog_times";//更新对话框

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences("preference_1", Context.MODE_PRIVATE);
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
}
