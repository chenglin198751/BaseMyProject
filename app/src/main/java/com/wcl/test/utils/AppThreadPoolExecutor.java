package com.wcl.test.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 通用线程池工具类
 * 默认线程池配置：核心线程数 2，最大线程数 8，队列容量 100，线程空闲 60 秒回收。
 */
public class AppThreadPoolExecutor {

    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final int QUEUE_CAPACITY = 100;

    private static volatile ThreadPoolExecutor executor;

    /**
     * 获取全局单例线程池
     */
    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            synchronized (AppThreadPoolExecutor.class) {
                if (executor == null) {
                    executor = new ThreadPoolExecutor(
                            CORE_POOL_SIZE,
                            MAX_POOL_SIZE,
                            KEEP_ALIVE_SECONDS,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                            new ThreadPoolExecutor.AbortPolicy()
                    );
                    executor.allowCoreThreadTimeOut(false);
                }
            }
        }
        return executor;
    }

    /**
     * 优雅关闭线程池（等待任务执行完成）
     */
    public static void shutdown() {
        if (executor != null) {
            synchronized (AppThreadPoolExecutor.class) {
                if (executor != null) {
                    executor.shutdown();
                    executor = null;
                }
            }
        }
    }

    /**
     * 强制关闭线程池（中断任务）
     */
    public static void shutdownNow() {
        if (executor != null) {
            synchronized (AppThreadPoolExecutor.class) {
                if (executor != null) {
                    executor.shutdownNow();
                    executor = null;
                }
            }
        }
    }

}