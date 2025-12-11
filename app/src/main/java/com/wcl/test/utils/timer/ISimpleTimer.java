package com.wcl.test.utils.timer;

public interface ISimpleTimer {

    /**
     * 设置首次延迟
     */
    ISimpleTimer setDelay(long delayMs);

    /**
     * 设置执行时间间隔
     */
    ISimpleTimer setInterval(long interval);

    /**
     * 设置每次触发回调
     */
    ISimpleTimer onTick(onTickListener callback);

    /**
     * 启动定时器
     */
    void start();

    /**
     * 停止定时器
     */
    void stop();
}
