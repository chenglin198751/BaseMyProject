package com.wcl.test.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.utils.AppLogUtils;

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
                + "create_time INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000), "
                + "update_time INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000)"
                + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void saveTask(DownloadTask task) {
        if (task == null || task.taskId == null || task.taskId.isEmpty()) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        String task_json = gson.toJson(task);
        long now = System.currentTimeMillis();

        // 先尝试更新，只更新 task_json 和 update_time，不改变 _id 和 create_time
        ContentValues updateCv = new ContentValues();
        updateCv.put("task_json", task_json);
        updateCv.put("update_time", now);
        int rows = db.update(TABLE_NAME, updateCv, "task_id=?", new String[]{task.taskId});

        // 不存在则插入（_id 自增，时间戳由 DEFAULT 自动填充）
        if (rows <= 0) {
            ContentValues cv = new ContentValues();
            cv.put("task_id", task.taskId);
            cv.put("task_json", task_json);
            // 回填 DB 层生成的字段
            task._id = db.insert(TABLE_NAME, null, cv);
            task.create_time = now;
        }
        task.update_time = now;
    }

    public DownloadTask loadTask(String taskId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json", "_id", "create_time", "update_time"}, "task_id=?", new String[]{taskId}, null, null, null)) {
            if (c.moveToFirst()) {
                try {
                    String json = c.getString(0);
                    DownloadTask task = gson.fromJson(json, DownloadTask.class);
                    task._id = c.getLong(1);
                    task.create_time = c.getLong(2);
                    task.update_time = c.getLong(3);
                    return task;
                } catch (Exception e) {
                    AppLogUtils.e("DownloadDBHelper", "Failed:" + e);
                }
            }
            return null;
        }
    }

    public List<DownloadTask> loadAllTasks() {
        List<DownloadTask> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json", "_id", "create_time", "update_time"}, null, null, null, null, "_id DESC")) {
            while (c.moveToNext()) {
                try {
                    String json = c.getString(0);
                    DownloadTask task = gson.fromJson(json, DownloadTask.class);
                    task._id = c.getLong(1);
                    task.create_time = c.getLong(2);
                    task.update_time = c.getLong(3);
                    list.add(task);
                } catch (Exception e) {
                    AppLogUtils.e("DownloadDBHelper", "Failed:" + e);
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
