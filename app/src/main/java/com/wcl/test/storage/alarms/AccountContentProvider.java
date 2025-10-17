package com.wcl.test.storage.alarms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class AccountContentProvider {
    private static final String TAG = "AccountContentProvider";

    private static final String ALARM_NAME = "account_data_alarm.mp3";

    /**
     * 向公共 Alarms 目录写入文本内容
     *
     * @param context Context
     * @param content 要写入的文本内容（UTF-8 编码）
     * @return 新文件的 Uri（失败返回 null）
     */
    public static Uri writeToAlarmsDir(Context context, String content) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Audio.Media.DISPLAY_NAME, ALARM_NAME);
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg");
        values.put(MediaStore.Audio.Media.IS_ALARM, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Alarms/");
        }

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
                        // 防止创建空文件
                        os.write(new byte[]{0});
                    }
                    os.flush();
                }
            }
            return uri;

        } catch (Exception e) {
            e.printStackTrace();
            if (uri != null) {
                // 写入失败删除残留
                resolver.delete(uri, null, null);
            }
            return null;
        }
    }

    /**
     * 从公共 Alarms 目录读取指定文件内容（UTF-8 编码）
     *
     * @param context Context
     * @return 文件内容字符串（失败返回 null）
     */
    public static String readFromAlarmsDir(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME
        };
        String selection = MediaStore.Audio.Media.DISPLAY_NAME + "=?";
        String[] selectionArgs = {ALARM_NAME};

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
                    return sb.toString().trim();
                }
            } else {
                Log.w(TAG, "未找到文件：" + ALARM_NAME);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}

