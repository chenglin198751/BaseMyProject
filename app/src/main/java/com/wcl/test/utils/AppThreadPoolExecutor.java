package com.wcl.test.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 通用线程池工具类
 * 默认线程池配置：核心线程数 2，最大线程数 8，队列容量 100，线程空闲 60 秒回收。
 * 可通过重载方法传入自定义配置。
 */
public class AppThreadPoolExecutor {
    private static final int DEFAULT_CORE_POOL_SIZE = 2;
    private static final int DEFAULT_MAX_POOL_SIZE = 8;
    private static final int DEFAULT_KEEP_ALIVE_SECONDS = 60;
    private static final int DEFAULT_QUEUE_CAPACITY = 100;

    private static volatile ThreadPoolExecutor mExecutor = null;

    /**
     * 获取全局单例线程池（默认配置）
     */
    public static ThreadPoolExecutor getExecutor() {
        return getExecutor(
                DEFAULT_CORE_POOL_SIZE,
                DEFAULT_MAX_POOL_SIZE,
                DEFAULT_KEEP_ALIVE_SECONDS,
                DEFAULT_QUEUE_CAPACITY,
                new ThreadPoolExecutor.DiscardPolicy() // 默认丢弃任务
        );
    }

    /**
     * 获取全局单例线程池（支持自定义参数）
     */
    public static ThreadPoolExecutor getExecutor(int corePoolSize,
                                                 int maxPoolSize,
                                                 int keepAliveSeconds,
                                                 int queueCapacity,
                                                 RejectedExecutionHandler handler) {
        if (mExecutor == null || mExecutor.isShutdown()) {
            synchronized (AppThreadPoolExecutor.class) {
                if (mExecutor == null || mExecutor.isShutdown()) {
                    ThreadFactory threadFactory = new NamedThreadFactory("AppThreadPool-");
                    mExecutor = new ThreadPoolExecutor(
                            corePoolSize,
                            maxPoolSize,
                            keepAliveSeconds,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(queueCapacity),
                            threadFactory,
                            handler
                    );
                    // 核心线程也能超时回收
                    mExecutor.allowCoreThreadTimeOut(true);
                }
            }
        }
        return mExecutor;
    }

    /**
     * 优雅关闭线程池（等待正在执行的任务完成）
     */
    public static void shutdown() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    /**
     * 强制关闭线程池（中断正在执行的任务）
     */
    public static void shutdownNow() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    /**
     * 自定义线程工厂：给线程命名
     */
    static class NamedThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        NamedThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + "thread-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            return t;
        }
    }
}
