package com.wcl.test.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenglin on 2018-1-19.
 */

public class GifCacheUtils {
    private static ExecutorService mGifThreadPool = null;

    public static ExecutorService getThreadPool() {
        if (mGifThreadPool == null) {
            synchronized (GifCacheUtils.class) {
                if (mGifThreadPool == null) {
                    mGifThreadPool = Executors.newFixedThreadPool(4);
                }
            }
        }
        return mGifThreadPool;
    }
}
