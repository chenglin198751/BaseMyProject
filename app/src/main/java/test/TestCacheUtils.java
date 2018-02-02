package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by chenglin on 2018-2-2.
 */

public class TestCacheUtils {
    //只取App 可用内存的十分之一
    private static ExecutorService mThreadPool = null;

    public static ExecutorService getThreadPool() {
        if (mThreadPool == null) {
            synchronized (utils.GifCacheUtils.class) {
                if (mThreadPool == null) {
                    mThreadPool = Executors.newFixedThreadPool(5);
                }
            }
        }
        return mThreadPool;
    }

    public static void shutdown() {
        if (mThreadPool != null) {
            mThreadPool.shutdown();
        }
    }
}
