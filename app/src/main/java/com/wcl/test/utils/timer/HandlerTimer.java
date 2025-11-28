package com.wcl.test.utils.timer;

import android.os.Handler;
import android.os.Looper;

public class HandlerTimer implements SimpleTimer {

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
    public SimpleTimer setDelay(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    @Override
    public SimpleTimer setInterval(long interval) {
        this.intervalMs = interval;
        return this;
    }

    @Override
    public SimpleTimer onTick(onTickListener callback) {
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
