package utils;

import android.os.Environment;

import com.google.gson.Gson;

/**
 * 公共常量类
 */
public class Constants {
    public static int screenWidth;
    public static int screenHeight;
    public final static Gson gson = new Gson();
    public static final String ACTION_GET_PHOTO_LIST = "ACTION_GET_PHOTO_LIST";
    public static final String KEY_PHOTO_LIST = "KEY_PHOTO_LIST";
}
