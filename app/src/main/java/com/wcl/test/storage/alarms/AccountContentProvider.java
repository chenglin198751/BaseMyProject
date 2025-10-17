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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * AccountContentProvider
 * 用于通过 MediaStore 在公共 Alarms 目录中读写共享账户数据文件，
 * 支持 Android 各版本的兼容性处理，无需存储权限。
 */
public class AccountContentProvider {

    private static final String TAG = "AccountContentProvider";
    private static final String ALARM_MP3_NAME = "account_data_alarm.mp3";

    /**
     * 向公共 Alarms 目录写入文本内容（兼容所有 Android 版本）
     *
     * @param context Context
     * @param content 要写入的文本内容（UTF-8 编码）
     * @return 新文件的 Uri（失败返回 null）
     */
    public static Uri writeToAlarmsDir(Context context, String content) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ✅ Android 10+ 使用 MediaStore 方式
            ContentResolver resolver = context.getContentResolver();
            ContentValues values = new ContentValues();

            values.put(MediaStore.Audio.Media.DISPLAY_NAME, ALARM_MP3_NAME);
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
            values.put(MediaStore.Audio.Media.IS_ALARM, 1);
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Alarms/");

            Uri uri = null;
            try {
                uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
                if (uri == null) {
                    Log.e(TAG, "插入 MediaStore 失败");
                    return null;
                }

                try (OutputStream os = resolver.openOutputStream(uri)) {
                    if (os != null) {
                        if (content != null && !content.isEmpty()) {
                            os.write(content.getBytes(StandardCharsets.UTF_8));
                        } else {
                            os.write(new byte[]{0});
                        }
                        os.flush();
                    }
                }
                Log.i(TAG, "写入成功 (Q+): " + uri);
                return uri;

            } catch (Exception e) {
                e.printStackTrace();
                if (uri != null) resolver.delete(uri, null, null);
                return null;
            }
        } else {
            // ⚙️ Android 9 及以下版本使用传统文件方式（需要 WRITE_EXTERNAL_STORAGE 权限）
            try {
                File alarmsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
                if (!alarmsDir.exists()) alarmsDir.mkdirs();

                File file = new File(alarmsDir, ALARM_MP3_NAME);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    if (content != null && !content.isEmpty()) {
                        fos.write(content.getBytes(StandardCharsets.UTF_8));
                    } else {
                        fos.write(new byte[]{0});
                    }
                    fos.flush();
                }
                Log.i(TAG, "写入成功 (legacy): " + file.getAbsolutePath());
                return Uri.fromFile(file);

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * 从公共 Alarms 目录读取指定文件内容（兼容所有 Android 版本）
     *
     * @param context Context
     * @return 文件内容字符串（失败返回 null）
     */
    public static String readFromAlarmsDir(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ✅ Android 10+ 使用 MediaStore 方式读取
            ContentResolver resolver = context.getContentResolver();
            Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.DISPLAY_NAME
            };
            String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";
            String[] selectionArgs = {ALARM_MP3_NAME};

            try (Cursor cursor = resolver.query(collection, projection, selection, selectionArgs, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    long id = cursor.getLong(idIndex);
                    Uri contentUri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                    try (InputStream is = resolver.openInputStream(contentUri);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line).append('\n');
                        }
                        String result = sb.toString().trim();
                        Log.i(TAG, "读取成功 (Q+): " + result);
                        return result;
                    }
                } else {
                    Log.w(TAG, "未找到文件：" + ALARM_MP3_NAME);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        } else {
            // ⚙️ Android 9 及以下使用 FileInputStream 读取
            try {
                File alarmsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_ALARMS);
                File file = new File(alarmsDir, ALARM_MP3_NAME);
                if (!file.exists()) {
                    Log.w(TAG, "文件不存在：" + file.getAbsolutePath());
                    return null;
                }

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append('\n');
                    }
                    String result = sb.toString().trim();
                    Log.i(TAG, "读取成功 (legacy): " + result);
                    return result;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
