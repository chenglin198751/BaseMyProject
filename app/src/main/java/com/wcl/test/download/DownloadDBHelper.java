package com.wcl.test.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载信息存储类（内部使用，不对外）
 */
class DownloadDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "okhttp3_download.db";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "download_task";
    private static final Gson gson = AppConstants.gson;

    public DownloadDBHelper() {
        super(BaseApp.getApp(), DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "task_id TEXT NOT NULL UNIQUE, "
                + "task_json TEXT NOT NULL, "
                + "create_time INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void saveTask(DownloadTask task) {
        SQLiteDatabase db = getWritableDatabase();
        // 先尝试更新，只更新 task_json 字段，不改变 _id 和 create_time
        ContentValues updateCv = new ContentValues();
        String task_json = gson.toJson(task);
        updateCv.put("task_json", task_json);
        int rows = db.update(TABLE_NAME, updateCv, "task_id=?", new String[]{task.taskId});

        // 如果不存在则插入（_id 自增，create_time 由 DEFAULT 自动填充）
        if (rows == 0) {
            ContentValues cv = new ContentValues();
            cv.put("task_id", task.taskId);
            cv.put("task_json", task_json);
            db.insert(TABLE_NAME, null, cv);
        }
    }

    public DownloadTask loadTask(String taskId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json"}, "task_id=?", new String[]{taskId}, null, null, null)) {
            if (c.moveToFirst()) {
                try {
                    String json = c.getString(0);
                    return gson.fromJson(json, DownloadTask.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    public List<DownloadTask> loadAllTasks() {
        List<DownloadTask> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json"}, null, null, null, null, null)) {
            while (c.moveToNext()) {
                try {
                    String json = c.getString(0);
                    list.add(gson.fromJson(json, DownloadTask.class));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return list;
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "task_id=?", new String[]{taskId});
    }
}
