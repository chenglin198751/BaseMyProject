package com.wcl.test.storage.alarms;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AccountContentProvider {

    /**
     * 向公共 Alarms 目录写入文件
     *
     * @param context  Context
     * @param fileName 文件名（如 "test.mp3"）
     * @param mimeType 文件类型（如 "audio/mpeg"）
     * @return 新文件的 Uri（失败返回 null）
     */
    public static Uri writeToAlarmsDir(Context context, String fileName, String mimeType) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();

        values.put(MediaStore.Audio.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Audio.Media.MIME_TYPE, mimeType);
        values.put(MediaStore.Audio.Media.IS_ALARM, 1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, "Alarms/");
        }

        Uri uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) {
            return null;
        }

        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(new byte[0]);
                outputStream.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            resolver.delete(uri, null, null);
            return null;
        }
        return uri;
    }

    /**
     * 读取公共 Alarms 目录下的音频文件
     *
     * @param context Context
     */
    public static List<String> getAlarmsList(Context context) {
        List<String> result = new ArrayList<>();
        ContentResolver resolver = context.getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
        };
        String selection = MediaStore.Audio.Media.IS_ALARM + " = 1";
        String sortOrder = MediaStore.Audio.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = resolver.query(uri, projection, selection, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                do {
                    String filePath = cursor.getString(pathIndex);
                    result.add(filePath);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
