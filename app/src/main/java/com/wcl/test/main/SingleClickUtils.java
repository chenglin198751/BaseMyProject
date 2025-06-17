package com.wcl.test.main;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class SingleClickUtils {
    // 使用原子类确保多线程安全
    private static final AtomicReference<String> mClickId = new AtomicReference<>(null);
    private static final AtomicLong mLastClickTime = new AtomicLong(0);
    // 时间间隔：比如2000毫秒内只能点击一次
    private final static long timeInterval = 2000L;

    public static boolean singleClick(final String click_id) {
        return singleClick(click_id, timeInterval);
    }

    public static boolean singleClick(final String click_id, final long interval) {
        if (click_id == null) {
            return false;
        }

        String currentId = mClickId.get();
        if (currentId == null) {
            if (!mClickId.compareAndSet(null, click_id)) {
                // 并发设置失败，重新获取并继续判断
                currentId = mClickId.get();
            } else {
                currentId = click_id;
            }
        }

        if (!click_id.equals(currentId)) {
            mLastClickTime.set(0);
            mClickId.set(click_id);
            currentId = click_id;
        }

        long nowTime = System.currentTimeMillis();
        long lastTime = mLastClickTime.get();

        if (nowTime - lastTime > interval) {
            mLastClickTime.set(nowTime);
            return true;
        }

        return false;
    }
}
