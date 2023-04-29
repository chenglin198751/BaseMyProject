package com.wcl.test.main;

import android.util.Log;

public class SingleClickUtils {
    private static long mLastClickTime;
    private static String mClickId = null;

    //时间间隔：比如2000毫秒内只能点击一次
    private final static long timeInterval = 2000L;

    public static boolean singleClick(final String click_id) {
        return singleClick(click_id, timeInterval);
    }

    public static boolean singleClick(final String click_id, final long interval) {
        if (mClickId == null) {
            mClickId = click_id;
        }
        if (!mClickId.equals(click_id)) {
            mLastClickTime = 0;
            mClickId = click_id;
        }
        long nowTime = System.currentTimeMillis();
        if (nowTime - mLastClickTime > interval) {
            Log.v("YxmeSDK", "---只能点击一次---");
            mLastClickTime = nowTime;
            return true;
        }
        Log.v("YxmeSDK", "---触发了多次点击---");
        return false;
    }

}