package com.wcl.test.base.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.wcl.test.BuildConfig;
import com.wcl.test.utils.AppLogUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 轻量 App 内跨进程事件 Provider（仅内存 sticky）
 * <p>
 * 注意：
 * - AndroidManifest 中必须注册（exported="false"）。
 * - 仅适用于“状态同步”，不保证事件不丢失。
 */
public class AppEventProvider extends ContentProvider {
    private static final String TAG = "AppEventProvider";
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.eventbus";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final String METHOD_POST = "POST";
    public static final String METHOD_GET = "GET";
    private static final String KEY_STATUS = "status";
    private static final String KEY_ERROR = "error";
    private static final String KEY_HAS_DATA = "has_data";
    private static final String KEY_DATA = "data";

    private static final Pattern SAFE_EVENT_PATTERN = Pattern.compile("[0-9A-Za-z_.\\-]+");
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

            String event = sanitizeEventName(arg);
            if (event == null) {
                return buildError("invalid_event");
            }

            if (METHOD_POST.equals(method)) {
                Bundle payload = (extras == null) ? Bundle.EMPTY : new Bundle(extras);
                stickyMap.put(event, payload);
                Bundle out = new Bundle();
                out.putString(KEY_STATUS, "ok");
                return out;

            } else if (METHOD_GET.equals(method)) {
                Bundle stored = stickyMap.get(event);
                Bundle out = new Bundle();
                if (stored == null) {
                    out.putBoolean(KEY_HAS_DATA, false);
                } else {
                    out.putBoolean(KEY_HAS_DATA, true);
                    out.putBundle(KEY_DATA, new Bundle(stored));
                }
                return out;
            } else {
                return buildError("unsupported_method");
            }

        } catch (Throwable t) {
            AppLogUtils.e(TAG, "call error:" + t);
            return buildError("exception");
        }
    }

    private Bundle buildError(String code) {
        Bundle b = new Bundle();
        b.putString(KEY_ERROR, code);
        return b;
    }

    /**
     * 限定 event 名只允许 [0-9a-zA-Z_.-]，长度 <= 128
     */
    public static String sanitizeEventName(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty() || s.length() > 128) return null;
        if (!SAFE_EVENT_PATTERN.matcher(s).matches()) return null;
        return s;
    }

    // 以下 API 不使用
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
