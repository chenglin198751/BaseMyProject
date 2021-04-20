package cheerly.mybaseproject.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.hjq.gson.factory.GsonFactory;

import cheerly.mybaseproject.base.BaseApp;

/**
 * 公共常量类
 */
public class Constants {
    private static int screenWidth;
    private static int screenHeight;

    public final static Gson gson = GsonFactory.getSingletonGson();

    public static final String ACTION_GET_PHOTO_LIST = "ACTION_GET_PHOTO_LIST";
    public static final String KEY_PHOTO_LIST = "KEY_PHOTO_LIST";

    static {
        Display display = ((WindowManager) BaseApp.getApp().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Constants.screenWidth = display.getWidth();
        Constants.screenHeight = display.getHeight();
    }

    public static int getScreenWidth() {
        return screenWidth;
    }

    public static int getScreenHeight() {
        return screenHeight;
    }
}
