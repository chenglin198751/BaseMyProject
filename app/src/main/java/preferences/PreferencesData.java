package preferences;

import android.content.Context;
import android.content.SharedPreferences;

import base.MyApplication;

public class PreferencesData {

    private static SharedPreferences getPreferences(final Context context) {
        return context.getSharedPreferences("preference_1", Context.MODE_PRIVATE);
    }

    public static void clear(){
        getPreferences(MyApplication.getApp()).edit().clear().commit();
    }

    /**
     * 测试key
     */
    private static final String testKey = "testKey";

    public static void setTest(String value) {
        SharedPreferences prefs = getPreferences(MyApplication.getApp());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(testKey, value);
        editor.commit();
    }

    public static String getTest() {
        return getPreferences(MyApplication.getApp()).getString(testKey, "");
    }

}
