package cheerly.mybaseproject.utils;

import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import cheerly.mybaseproject.base.BaseApp;
import cheerly.mybaseproject.httpwork.BooleanDefaultAdapter;
import cheerly.mybaseproject.httpwork.DoubleDefault0Adapter;
import cheerly.mybaseproject.httpwork.FloatDefault0Adapter;
import cheerly.mybaseproject.httpwork.IntegerDefault0Adapter;
import cheerly.mybaseproject.httpwork.LongDefault0Adapter;

/**
 * 公共常量类
 */
public class Constants {
    private static int screenWidth;
    private static int screenHeight;

    public final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Integer.class, new IntegerDefault0Adapter())
            .registerTypeAdapter(int.class, new IntegerDefault0Adapter())
            .registerTypeAdapter(Double.class, new DoubleDefault0Adapter())
            .registerTypeAdapter(double.class, new DoubleDefault0Adapter())
            .registerTypeAdapter(Long.class, new LongDefault0Adapter())
            .registerTypeAdapter(long.class, new LongDefault0Adapter())
            .registerTypeAdapter(Float.class, new FloatDefault0Adapter())
            .registerTypeAdapter(float.class, new FloatDefault0Adapter())
            .registerTypeAdapter(Boolean.class, new BooleanDefaultAdapter())
            .registerTypeAdapter(boolean.class, new BooleanDefaultAdapter())
            .create();

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
