package com.wcl.test.storage.alarms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 用于在公共 Alarms 目录中读写共享数据
 * Android 10+ 使用 MediaStore
 * Android 9 及以下使用 File
 */
public class AccountContentProvider {

    private static final String TAG = "AccountContentProvider";

    // 文件名（故意伪装 mp3）
    private static final String FILE_NAME = "account_data_alarm.mp3";

    private AccountContentProvider() {
    }

    /**
     * 写入数据
     */
    public static synchronized Uri writeToAlarmsDir(Context context, String content) {
        if (context == null) {
            return null;
        }

        if (content == null) {
            content = "";
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return writeByMediaStore(context, content);
        } else {
            return writeByFile(content);
        }
    }

    /**
     * 读取数据
     */
    public static synchronized String readFromAlarmsDir(Context context) {
        if (context == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return readByMediaStore(context);
        } else {
            return readByFile();
        }
    }

    /**
     * Android 10+ 写入
     */
    private static Uri writeByMediaStore(Context context, String content) {
        ContentResolver resolver = context.getContentResolver();
        Uri oldUri = findFileUri(context);

        try {
            // 已存在 -> 直接覆盖写入
            if (oldUri != null) {
                OutputStream os = resolver.openOutputStream(oldUri, "wt");
                try (os) {
                    if (os == null) {
                        return null;
                    }
                    os.write(content.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                Log.i(TAG, "覆盖写入成功: " + oldUri);
                return oldUri;
            }

            // 不存在 -> 新建
            ContentValues values = new ContentValues();
            values.put(MediaStore.Audio.Media.DISPLAY_NAME, FILE_NAME);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
            values.put(MediaStore.Audio.Media.IS_ALARM, 1);
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_ALARMS);
            Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Log.e(TAG, "insert MediaStore failed");
                return null;
            }

            try {
                OutputStream os = resolver.openOutputStream(uri);
                try (os) {
                    if (os == null) {
                        resolver.delete(uri, null, null);
                        return null;
                    }
                    os.write(content.getBytes(StandardCharsets.UTF_8));
                    os.flush();
                }

                Log.i(TAG, "写入成功: " + uri);
                return uri;

            } catch (Exception e) {
                resolver.delete(uri, null, null);
                throw e;
            }
        } catch (Exception e) {
            Log.e(TAG, "writeByMediaStore error", e);
            return null;
        }
    }

    /**
     * Android 10+ 读取
     */
    private static String readByMediaStore(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = findFileUri(context);

        if (uri == null) {
            Log.w(TAG, "文件不存在");
            return null;
        }

        try {
            InputStream is = resolver.openInputStream(uri);
            try (is) {
                if (is == null) {
                    return null;
                }
                return readStream(is);
            }
        } catch (Exception e) {
            Log.e(TAG, "readByMediaStore error", e);
            return null;
        }
    }

    /**
     * 查询文件 Uri
     */
    private static Uri findFileUri(Context context) {
        ContentResolver resolver = context.getContentResolver();
        String[] projection = {MediaStore.Audio.Media._ID};
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";

        String[] selectionArgs = {FILE_NAME};

        try (Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                MediaStore.Audio.Media.DATE_MODIFIED + " DESC"
        )) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                return Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            }
        } catch (Exception e) {
            Log.e(TAG, "findFileUri error", e);
        }

        return null;
    }

    /**
     * Android 9 及以下写入
     */
    private static Uri writeByFile(String content) {
        try {
            File alarmsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
            if (!alarmsDir.exists()) {
                alarmsDir.mkdirs();
            }

            File file = new File(alarmsDir, FILE_NAME);
            try (FileOutputStream fos = new FileOutputStream(file, false)) {
                fos.write(content.getBytes(StandardCharsets.UTF_8));
                fos.flush();
            }
            Log.i(TAG, "写入成功: " + file.getAbsolutePath());
            return Uri.fromFile(file);
        } catch (Exception e) {
            Log.e(TAG, "writeByFile error", e);
            return null;
        }
    }

    /**
     * Android 9 及以下读取
     */
    private static String readByFile() {
        try {
            File alarmsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
            File file = new File(alarmsDir, FILE_NAME);
            if (!file.exists()) {
                Log.w(TAG, "文件不存在");
                return null;
            }
            try (InputStream is = new FileInputStream(file)) {
                return readStream(is);
            }
        } catch (Exception e) {
            Log.e(TAG, "readByFile error", e);
            return null;
        }
    }

    /**
     * 读取流
     */
    private static String readStream(InputStream is) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } else {
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bas.write(buffer, 0, len);
            }
            return bas.toString(StandardCharsets.UTF_8.name());
        }
    }
}