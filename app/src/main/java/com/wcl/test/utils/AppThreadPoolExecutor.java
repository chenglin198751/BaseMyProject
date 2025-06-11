package com.wcl.test.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 创建一个线程池，此线程池最大线程数量是4个，如果60秒内没有使用，就自动关闭。
 * 慎用，否则某些业务会被阻塞。目前只有下载模块用到此线程池。
 */
public class AppThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int IDLE_TIME = 60;
    private static ThreadPoolExecutor mExecutor = null;

    public static ThreadPoolExecutor getExecutor() {
        if (mExecutor == null || mExecutor.isTerminated()) {
            mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, IDLE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
            mExecutor.allowCoreThreadTimeOut(true);
        }
        return mExecutor;
    }
}
