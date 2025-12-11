package com.wcl.test.base.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.wcl.test.BuildConfig;
import com.wcl.test.utils.AppLogUtils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 高性能、轻量的 App 内跨进程事件 Provider（加强版）。
 * 注意：
 * - AndroidManifest 中必须注册（exported="false"）。
 * - Provider 内不得做耗时操作。
 */
public class AppEventProvider extends ContentProvider {
    private static final String TAG = "AppEventProvider";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.eventbus";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";

    private static final ConcurrentHashMap<String, Bundle> stickyMap = new ConcurrentHashMap<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Bundle call(String method, String arg, Bundle extras) {
        try {
            if (TextUtils.isEmpty(method) || TextUtils.isEmpty(arg)) {
                return buildError("bad_args");
            }

            // 简单校验 event 名（避免注入和 path 问题）
            String event = sanitizeEventName(arg);
            if (event == null) return buildError("invalid_event");

            if (METHOD_POST.equals(method)) {
                // POST：写入内存（深拷贝 Bundle，避免外部修改）
                Bundle payload = (extras == null) ? null : new Bundle(extras);
                if (payload == null) payload = Bundle.EMPTY;

                stickyMap.put(event, payload);

                Bundle out = new Bundle();
                out.putString("status", "ok");
                return out;
            } else if (METHOD_GET.equals(method)) {
                // GET：返回深拷贝的 Bundle（或 null 表示无数据）
                Bundle stored = stickyMap.get(event);
                if (stored == null) return null;
                return new Bundle(stored);
            } else {
                return buildError("unsupported_method");
            }
        } catch (Throwable t) {
            AppLogUtils.e(TAG, "call error:" + t);
            return buildError("exception");
        }
    }

    // ---------- helpers ----------
    private Bundle buildError(String code) {
        Bundle b = new Bundle();
        b.putString("error", code);
        return b;
    }

    /**
     * 限定 event 名只允许 [0-9a-zA-Z_.-] 的字符，且长度限制
     */
    private String sanitizeEventName(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || s.length() > 128) return null;
        // 仅允许安全字符（可根据需要放宽）
        if (!s.matches("[0-9A-Za-z_.\\-]+")) return null;
        return s;
    }

    // 以下 API 不再使用，返回默认值
    @Override
    public android.database.Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
