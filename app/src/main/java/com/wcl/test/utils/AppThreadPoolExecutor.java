package com.wcl.test.utils;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 创建一个线程池，此线程池最大线程数量是8个，如果60秒内没有使用，就自动关闭。
 * 慎用，否则某些业务会被阻塞。目前只有下载模块用到此线程池。
 */
public class AppThreadPoolExecutor {
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAX_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_SECONDS = 60;
    private static final int QUEUE_CAPACITY = 100;

    private static volatile ThreadPoolExecutor mExecutor = null;

    public static ThreadPoolExecutor getExecutor() {
        if (mExecutor == null || mExecutor.isTerminated()) {
            synchronized (AppThreadPoolExecutor.class) {
                if (mExecutor == null || mExecutor.isTerminated()) {
                    ThreadFactory threadFactory = new NamedThreadFactory("AppThreadPool-");
                    mExecutor = new ThreadPoolExecutor(
                            CORE_POOL_SIZE,
                            MAX_POOL_SIZE,
                            KEEP_ALIVE_SECONDS,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
                            threadFactory,
                            new ThreadPoolExecutor.DiscardPolicy()
                    );
                    mExecutor.allowCoreThreadTimeOut(true);
                }
            }
        }
        return mExecutor;
    }

    /**
     * 关闭线程池，释放资源
     */
    public static void shutdown() {
        if (mExecutor != null && !mExecutor.isShutdown()) {
            mExecutor.shutdownNow();
            mExecutor = null;
        }
    }

    /**
     * 自定义线程工厂，用于给线程命名
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
