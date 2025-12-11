package com.wcl.test.base;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 全局事件总线，使用监听者模式
 */
public class EventBus {
    private final CopyOnWriteArrayList<OnEventBusListener> observers = new CopyOnWriteArrayList<>();
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    private static final class InstanceHolder {
        private static final EventBus INSTANCE = new EventBus();
    }

    private EventBus() {
    }

    public static EventBus instance() {
        return InstanceHolder.INSTANCE;
    }


    /**
     * 注册监听者
     */
    void register(OnEventBusListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null when registering.");
        }
        observers.addIfAbsent(observer);
    }

    /**
     * 取消注册监听者
     */
    void unregister(OnEventBusListener observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer cannot be null when unregistering.");
        }
        observers.remove(observer);
    }

    /**
     * 发送事件（主线程分发）
     */
    public static void post(String eventKey, Object data) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            instance().dispatch(eventKey, data);
        } else {
            MAIN_HANDLER.post(() -> instance().dispatch(eventKey, data));
        }
    }

    /**
     * 延迟发送事件（主线程分发）
     */
    public static void postDelay(String eventKey, Object data, long delayMillis) {
        MAIN_HANDLER.postDelayed(() -> instance().dispatch(eventKey, data), delayMillis);
    }

    /**
     * 内部分发逻辑
     */
    private void dispatch(String eventKey, Object data) {
        for (OnEventBusListener observer : observers) {
            try {
                observer.onEvent(eventKey, data);
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
