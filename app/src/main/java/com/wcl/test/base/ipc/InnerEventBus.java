package com.wcl.test.base.ipc;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppLogUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ----目前不用 2026-01-13----
 * 使用ContentProvider实现的轻量级，限定在app内跨进程通信方案
 */
class InnerEventBus {
    private static final String TAG = "InnerEventBus";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface EventCallback {
        void onEvent(Bundle data);
    }

    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<EventCallback>> localObservers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ContentObserver> contentObservers = new ConcurrentHashMap<>();

    private static Uri getEventUri(String event) {
        return Uri.withAppendedPath(AppEventProvider.BASE_URI, event);
    }

    /**
     * 发送跨进程事件
     */
    static void post(String event, Bundle data) {
        String safeEvent = AppEventProvider.sanitizeEventName(event);
        if (safeEvent == null) {
            AppLogUtils.w(TAG, "invalid event: " + event);
            return;
        }

        ContentResolver resolver = BaseApp.getApp().getContentResolver();
        try {
            Bundle extras = (data == null) ? new Bundle() : new Bundle(data);
            Bundle out = resolver.call(AppEventProvider.BASE_URI, AppEventProvider.METHOD_POST, safeEvent, extras);
            if (out != null && out.containsKey("error")) {
                AppLogUtils.w(TAG, "provider error: " + out.getString("error"));
            }
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "post call error:" + t);
        }

        try {
            resolver.notifyChange(getEventUri(safeEvent), null);
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "notifyChange error:" + t);
        }
    }

    /**
     * 注册监听
     */
    static void register(String event, EventCallback callback) {
        if (callback == null)
            throw new IllegalArgumentException("callback null");

        String safeEvent = AppEventProvider.sanitizeEventName(event);
        if (safeEvent == null)
            throw new IllegalArgumentException("invalid event");

        CopyOnWriteArrayList<EventCallback> list = localObservers.computeIfAbsent(safeEvent, k -> new CopyOnWriteArrayList<>());
        list.addIfAbsent(callback);

        contentObservers.computeIfAbsent(safeEvent, e -> {
            ContentResolver resolver = BaseApp.getApp().getContentResolver();
            EventObserver obs = new EventObserver(e, MAIN, resolver);
            resolver.registerContentObserver(getEventUri(e), false, obs);
            return obs;
        });
    }

    /**
     * 取消注册
     */
    static void unregister(String event, EventCallback callback) {
        if (callback == null) return;
        String safeEvent = AppEventProvider.sanitizeEventName(event);
        if (safeEvent == null) return;
        CopyOnWriteArrayList<EventCallback> list = localObservers.get(safeEvent);
        if (list == null) return;
        list.remove(callback);

        if (list.isEmpty()) {
            localObservers.remove(safeEvent);
            ContentObserver obs = contentObservers.remove(safeEvent);

            if (obs != null) {
                try {
                    BaseApp.getApp().getContentResolver().unregisterContentObserver(obs);
                } catch (Throwable ignore) {
                }
            }
        }
    }

    // ---------------- observer ----------------

    static class EventObserver extends ContentObserver {

        private final String event;
        private final ContentResolver resolver;

        EventObserver(String event, Handler handler, ContentResolver resolver) {
            super(handler);
            this.event = event;
            this.resolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            try {
                Bundle resp = resolver.call(AppEventProvider.BASE_URI, AppEventProvider.METHOD_GET, event, null);
                if (resp == null) return;
                if (!resp.getBoolean("has_data")) return;

                Bundle data = resp.getBundle("data");
                if (data == null) return;

                CopyOnWriteArrayList<EventCallback> list = localObservers.get(event);

                if (list == null || list.isEmpty())
                    return;

                for (EventCallback cb : list) {
                    try {
                        cb.onEvent(new Bundle(data));
                    } catch (Throwable t) {
                        AppLogUtils.w(TAG, "callback error:" + t);
                    }
                }
            } catch (Throwable t) {
                AppLogUtils.e(TAG, "observer error:" + t);
            }
        }
    }
}
