package com.wcl.test.utils.timer;

import android.os.Handler;
import android.os.Looper;

/**
 * 短间隔循环任务，推荐 1~10 秒执行一次
 */
public class HandlerTimer implements ISimpleTimer {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private onTickListener callback;
    private long delayMs = 0;
    private long intervalMs = 0;

    private boolean isRunning = false;

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            if (callback != null) {
                callback.onTick();
            }

            // 循环执行下一次
            handler.postDelayed(this, intervalMs);
        }
    };

    public HandlerTimer() {
    }

    @Override
    public ISimpleTimer setDelay(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    @Override
    public ISimpleTimer setInterval(long interval) {
        this.intervalMs = interval;
        return this;
    }

    @Override
    public ISimpleTimer onTick(onTickListener callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void start() {
        stop();
        isRunning = true;
        handler.postDelayed(runnable, delayMs);
    }

    @Override
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(runnable);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
