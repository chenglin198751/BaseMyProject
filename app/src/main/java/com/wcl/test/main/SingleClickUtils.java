package com.wcl.test.main;

import android.os.SystemClock;
import android.text.TextUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 防止快速重复点击的工具类
 */
public class SingleClickUtils {
    private static final ConcurrentHashMap<String, AtomicLong> clickMap = new ConcurrentHashMap<>();
    private static final int DEFAULT_INTERVAL_MS = 1000;

    public static boolean isSingle(final String clickId) {
        return isSingle(clickId, DEFAULT_INTERVAL_MS);
    }

    public static boolean isSingle(final String clickId, final int interval) {
        if (TextUtils.isEmpty(clickId) || interval <= 0) {
            return false;
        }

        long nowTime = SystemClock.elapsedRealtime();
        AtomicLong lastTime = clickMap.computeIfAbsent(clickId, k -> new AtomicLong(0));
        long previousTime = lastTime.get();
        if (nowTime - previousTime > interval) {
            return lastTime.compareAndSet(previousTime, nowTime);
        }

        return false;
    }
}