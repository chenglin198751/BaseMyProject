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
     * 每次触发回调
     */
    ISimpleTimer onTick(onTickListener callback);

    /**
     * 设置执行次数上限
     */
    ISimpleTimer setRepeatCount(int count);

    /**
     * 启动定时器
     */
    void start();

    /**
     * 停止定时器
     */
    void stop();
}
