package com.wcl.test.base.ipc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import com.wcl.test.BuildConfig;

import java.util.concurrent.ConcurrentHashMap;

/**
 * App 内多进程事件总线的 Provider，所有事件数据保存在内存 Map 中。
 * exported=false → 外部 App 无法访问
 */
public class AppEventProvider extends ContentProvider {
    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.eventbus";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    private static final int EVENT = 1;

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

    // 存储每个事件的最新数据（JSON、String、或任意序列化格式）
    private static final ConcurrentHashMap<String, String> eventDataMap = new ConcurrentHashMap<>();

    static {
        matcher.addURI(AUTHORITY, "*", EVENT);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * 外部通过 post 时，会调用这个方法写入事件数据
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int code = matcher.match(uri);
        if (code == EVENT) {
            String event = uri.getLastPathSegment();
            String data = values.getAsString("data");
            eventDataMap.put(event, data);
            return uri;
        }
        return null;
    }

    /**
     * 事件监听者通过 query 获取事件数据
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int code = matcher.match(uri);
        if (code == EVENT) {
            String event = uri.getLastPathSegment();
            String data = eventDataMap.get(event);

            MatrixCursor cursor = new MatrixCursor(new String[]{"data"});
            cursor.addRow(new Object[]{data});
            return cursor;
        }
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

    @Override
    public String getType(Uri uri) {
        return null;
    }
}
