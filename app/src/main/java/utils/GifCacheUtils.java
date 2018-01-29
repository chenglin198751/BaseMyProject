package utils;

import android.util.LruCache;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import pl.droidsonroids.gif.GifDrawable;

/**
 * Created by chenglin on 2018-1-19.
 */

public class GifCacheUtils {
    //只取App 可用内存的十分之一
    private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024 / 10);//单位KB
    private static volatile LruCache<String, GifDrawable> mGifCache = null;
    private static ExecutorService mGifThreadPool = null;

    public static void put(String key, GifDrawable gifDrawable) {
        if (mGifCache == null) {
            synchronized (GifCacheUtils.class) {
                if (mGifCache == null) {
                    mGifCache = new LruCache<String, GifDrawable>(CACHE_SIZE) {
                        @Override
                        protected int sizeOf(String key, GifDrawable gifDrawable) {
                            return (int) (gifDrawable.getAllocationByteCount() / 1024);//单位KB
                        }
                    };
                }
            }
        }
        mGifCache.put(key, gifDrawable);
    }


    public static GifDrawable get(String key) {
        if (mGifCache != null) {
            return mGifCache.get(key);
        } else {
            return null;
        }
    }

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
