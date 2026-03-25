package com.wcl.test.base.ipc;

import android.os.Bundle;

/**
 * ---- 2026-01-13 目前不用----
 * 使用ContentProvider实现的轻量级，限定在app内跨进程通信方案
 */
public class EventBus2 {

    /**
     * 发送跨进程事件
     */
    public static void post(String event, Bundle data) {
        InnerEventBus.post(event, data);
    }

    /**
     * 注册跨进程事件监听器
     */
    public static void register(String event, InnerEventBus.EventCallback callback) {
        InnerEventBus.register(event, callback);
    }

    /**
     * 取消注册跨进程事件监听器
     */
    public static void unregister(String event, InnerEventBus.EventCallback callback) {
        InnerEventBus.unregister(event, callback);
    }

}
