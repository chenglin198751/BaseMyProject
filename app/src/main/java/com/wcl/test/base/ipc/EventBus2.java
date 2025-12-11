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
 * 使用ContentProvider实现的轻量级，限定在app内跨进程通信方案
 */
public class EventBus2 {
    private static final String TAG = "EventBus2";
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface EventCallback {
        void onEvent(Bundle data);
    }

    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<EventCallback>> localObservers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ContentObserver> contentObservers = new ConcurrentHashMap<>();

    private static Uri getEventUri(String event) {
        return Uri.withAppendedPath(AppEventProvider.BASE_URI, event);
    }

    // ---------------- post ----------------
    public static void post(String event, Bundle data) {
        ContentResolver resolver = BaseApp.getApp().getContentResolver();

        try {
            Bundle extras = (data == null) ? new Bundle() : new Bundle(data); // shallow copy
            Bundle out = resolver.call(AppEventProvider.BASE_URI, AppEventProvider.METHOD_POST, event, extras);
            if (out != null && out.containsKey("error")) {
                AppLogUtils.w(TAG, "provider returned error: " + out.getString("error"));
            }
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "post call error:" + t);
        }

        try {
            Uri uri = getEventUri(event);
            resolver.notifyChange(uri, null);
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "notifyChange error:" + t);
        }
    }

    // ---------------- register ----------------
    public static void register(String event, EventCallback callback) {
        if (event == null || callback == null) throw new IllegalArgumentException("args null");

        CopyOnWriteArrayList<EventCallback> list = localObservers.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>());
        list.addIfAbsent(callback);

        contentObservers.computeIfAbsent(event, e -> {
            Uri uri = getEventUri(e);
            EventObserver obs = new EventObserver(e, MAIN, BaseApp.getApp().getContentResolver());
            BaseApp.getApp().getContentResolver().registerContentObserver(uri, false, obs);
            return obs;
        });
    }

    public static void unregister(String event, EventCallback callback) {
        if (event == null || callback == null) throw new IllegalArgumentException("args null");
        CopyOnWriteArrayList<EventCallback> list = localObservers.get(event);
        if (list != null) list.remove(callback);
    }

    // ---------------- EventObserver ----------------
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
            super.onChange(selfChange, uri);
            try {
                Bundle resp = resolver.call(AppEventProvider.BASE_URI, AppEventProvider.METHOD_GET, event, null);
                if (resp == null) {
                    return;
                }

                CopyOnWriteArrayList<EventCallback> list = localObservers.get(event);
                if (list == null || list.isEmpty()) return;
                for (EventCallback cb : list) {
                    Bundle payload = new Bundle(resp);
                    MAIN.post(() -> {
                        try {
                            cb.onEvent(payload);
                        } catch (Throwable t) {
                            AppLogUtils.w(TAG, "callback error:" + t);
                        }
                    });
                }
            } catch (Throwable t) {
                AppLogUtils.e(TAG, "observer onChange error:" + t);
            }
        }
    }
}
