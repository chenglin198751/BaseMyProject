package com.wcl.test.base;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局事件总线，使用监听者模式
 */
public class EventBus2 {
    private final CopyOnWriteArrayList<OnBroadcastListener> observers = new CopyOnWriteArrayList<>();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final class InstanceHolder {
        private static final EventBus2 INSTANCE = new EventBus2();
    }

    private EventBus2() {
    }

    public static EventBus2 get() {
        return InstanceHolder.INSTANCE;
    }


    /**
     * 注册监听者
     */
    void register(OnBroadcastListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null when registering.");
        }
        observers.addIfAbsent(observer);
    }

    /**
     * 取消注册监听者
     */
    void unregister(OnBroadcastListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null when unregistering.");
        }
        observers.remove(observer);
    }

    /**
     * 发送事件（主线程分发）
     */
    public void post(String eventKey, Object data) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            dispatch(eventKey, data);
        } else {
            MAIN_HANDLER.post(() -> dispatch(eventKey, data));
        }
    }

    /**
     * 延迟发送事件（主线程分发）
     */
    public void postDelay(String eventKey, Object data, long delayMillis) {
        MAIN_HANDLER.postDelayed(() -> dispatch(eventKey, data), delayMillis);
    }

    /**
     * 内部分发逻辑
     */
    private void dispatch(String eventKey, Object data) {
        for (OnBroadcastListener observer : observers) {
            try {
                observer.onBroadcastReceiver(eventKey, data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清除所有监听者
     */
    public void clear() {
        observers.clear();
    }

    /**
     * 获取当前注册监听者数量（调试用）
     */
    public int getObserverCount() {
        return observers.size();
    }
}
