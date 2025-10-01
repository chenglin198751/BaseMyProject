package com.wcl.test.main;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class SingleClickUtils {
    private static final ConcurrentHashMap<String, AtomicLong> clickMap = new ConcurrentHashMap<>();
    private static final long DEFAULT_INTERVAL = 2000L;

    public static boolean singleClick(final String click_id) {
        return singleClick(click_id, DEFAULT_INTERVAL);
    }

    public static boolean singleClick(final String click_id, final long interval) {
        if (click_id == null) {
            return false;
        }

        long nowTime = System.currentTimeMillis();
        AtomicLong lastTime = clickMap.get(click_id);
        if (lastTime == null) {
            lastTime = new AtomicLong(0);
            AtomicLong existing = clickMap.putIfAbsent(click_id, lastTime);
            if (existing != null) {
                lastTime = existing;
            }
        }
        long previousTime = lastTime.get();

        if (nowTime - previousTime > interval) {
            lastTime.set(nowTime);
            return true;
        }
        return false;
    }
}