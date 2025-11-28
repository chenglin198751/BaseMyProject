package com.wcl.test.utils.timer;

public interface SimpleTimer {

    /**
     * 设置首次延迟
     */
    SimpleTimer setDelay(long delayMs);

    /**
     * 设置执行时间间隔
     */
    SimpleTimer setInterval(long interval);

    /**
     * 设置每次触发回调
     */
    SimpleTimer onTick(onTickListener callback);

    /**
     * 启动定时器
     */
    void start();

    /**
     * 停止定时器
     */
    void stop();
}
