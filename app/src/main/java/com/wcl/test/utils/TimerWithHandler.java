package com.wcl.test.utils;

import android.os.Handler;
import android.os.Looper;

public class TimerWithHandler {

    public interface OnTickListener {
        void onTick();
    }

    private final long intervalMillis;
    private final long startDelayMillis;
    private final OnTickListener listener;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;

    /**
     * @param intervalMillis   每次执行间隔
     * @param startDelayMillis 启动延迟
     * @param listener         监听
     */
    public TimerWithHandler(long intervalMillis, long startDelayMillis, OnTickListener listener) {
        this.intervalMillis = intervalMillis;
        this.startDelayMillis = startDelayMillis;
        this.listener = listener;
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;

            // 每次触发执行
            if (listener != null) {
                listener.onTick();
            }

            // 继续下一次执行
            handler.postDelayed(this, intervalMillis);
        }
    };

    /**
     * 开始定时器
     */
    public void start() {
        if (isRunning) return;
        isRunning = true;
        handler.postDelayed(runnable, startDelayMillis);
    }

    /**
     * 停止定时器
     */
    public void stop() {
        isRunning = false;
        handler.removeCallbacks(runnable);
    }

    /**
     * 定时器是否正在运行
     */
    public boolean isRunning() {
        return isRunning;
    }
}
