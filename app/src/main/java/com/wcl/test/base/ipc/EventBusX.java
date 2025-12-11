package com.wcl.test.base.ipc;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.wcl.test.base.BaseApp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 多进程事件总线（类似 EventBus 风格）
 * 支持跨进程广播，仅限当前 App 内。
 */
public class EventBusX {
    private static final ConcurrentHashMap<String, CopyOnWriteArrayList<EventCallback>> localObservers = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ContentObserver> contentObservers = new ConcurrentHashMap<>();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public interface EventCallback {
        void onEvent(String data);
    }


    private static Uri getEventUri(String event) {
        return Uri.withAppendedPath(AppEventProvider.BASE_URI, event);
    }

    /**
     * 发送消息，目前只支持String数据传递
     */
    public static void post(String event, String data) {
        Uri uri = getEventUri(event);
        ContentResolver resolver = BaseApp.getApp().getContentResolver();

        // 1. 写入数据到 Provider 的内存Map
        ContentValues cv = new ContentValues();
        cv.put("data", data);
        resolver.insert(uri, cv);

        // 2. 触发跨进程通知
        resolver.notifyChange(uri, null);
    }

    /**
     * 注册
     */
    public static void register(String event, EventCallback callback) {
        CopyOnWriteArrayList<EventCallback> list = localObservers.computeIfAbsent(event, k -> new CopyOnWriteArrayList<>());
        list.addIfAbsent(callback);
        contentObservers.computeIfAbsent(event, e -> {
            Uri uri = getEventUri(event);
            ContentObserver observer = new EventObserver(event, MAIN, BaseApp.getApp().getContentResolver());
            BaseApp.getApp().getContentResolver().registerContentObserver(uri, false, observer);
            return observer;
        });
    }

    /**
     * 取消注册
     */
    public static void unregister(String event, EventCallback callback) {
        CopyOnWriteArrayList<EventCallback> list = localObservers.get(event);
        if (list != null) {
            list.remove(callback);
        }
    }

    // -----------------------------------------
    //      内部类：ContentObserver 封装
    // -----------------------------------------
    static class EventObserver extends ContentObserver {

        private final String eventName;
        private final ContentResolver resolver;

        EventObserver(String event, Handler handler, ContentResolver resolver) {
            super(handler);
            this.eventName = event;
            this.resolver = resolver;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            // 读取 Provider 中的数据
            String data = null;
            try (var cursor = resolver.query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    data = cursor.getString(cursor.getColumnIndexOrThrow("data"));
                }
            }

            // 分发给当前进程的所有监听者
            CopyOnWriteArrayList<EventCallback> list = localObservers.get(eventName);
            if (list != null && data != null) {
                for (EventCallback cb : list) {
                    final String data2 = data;
                    MAIN.post(() -> cb.onEvent(data2));
                }
            }
        }
    }
}
