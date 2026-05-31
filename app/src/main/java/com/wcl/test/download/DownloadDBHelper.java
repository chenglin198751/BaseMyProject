package com.wcl.test.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.wcl.test.base.BaseApp;
import com.wcl.test.utils.AppConstants;
import com.wcl.test.utils.AppLogUtils;

import java.io.File;
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
    private static volatile DownloadDBHelper sInstance;

    private DownloadDBHelper() {
        super(BaseApp.getApp(), DB_NAME, null, VERSION);
    }

    public static DownloadDBHelper ins() {
        if (sInstance == null) {
            synchronized (DownloadDBHelper.class) {
                if (sInstance == null) {
                    sInstance = new DownloadDBHelper();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "task_id TEXT NOT NULL UNIQUE, "
                + "task_json TEXT NOT NULL, "
                + "create_time INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000), "
                + "update_time INTEGER NOT NULL DEFAULT (strftime('%s','now') * 1000), "
                + "extra TEXT"
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

        // 先尝试更新，只更新 task_json、update_time、extra，不改变 _id 和 create_time
        ContentValues updateCv = new ContentValues();
        updateCv.put("task_json", task_json);
        updateCv.put("update_time", now);
        updateCv.put("extra", !TextUtils.isEmpty(task.extra) ? task.extra : null);
        int rows = db.update(TABLE_NAME, updateCv, "task_id=?", new String[]{task.taskId});

        // 不存在则插入（_id 自增，时间戳由 DEFAULT 自动填充）
        if (rows <= 0) {
            ContentValues cv = new ContentValues();
            cv.put("task_id", task.taskId);
            cv.put("task_json", task_json);
            cv.put("extra", !TextUtils.isEmpty(task.extra) ? task.extra : null);
            // 回填 DB 层生成的字段
            task._id = db.insert(TABLE_NAME, null, cv);
            task.create_time = now;
        }
        task.update_time = now;
    }

    public DownloadTask loadTask(String taskId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json", "_id", "create_time", "update_time", "extra"}, "task_id=?", new String[]{taskId}, null, null, null)) {
            if (c.moveToFirst()) {
                try {
                    String json = c.getString(0);
                    DownloadTask task = gson.fromJson(json, DownloadTask.class);
                    task._id = c.getLong(1);
                    task.create_time = c.getLong(2);
                    task.update_time = c.getLong(3);
                    task.extra = c.getString(4);
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
        try (Cursor c = db.query(TABLE_NAME, new String[]{"task_json", "_id", "create_time", "update_time", "extra"}, null, null, null, null, "_id DESC")) {
            while (c.moveToNext()) {
                try {
                    String json = c.getString(0);
                    DownloadTask task = gson.fromJson(json, DownloadTask.class);
                    task._id = c.getLong(1);
                    task.create_time = c.getLong(2);
                    task.update_time = c.getLong(3);
                    task.extra = c.getString(4);
                    syncFromFileSystem(task);
                    list.add(task);
                } catch (Exception e) {
                    AppLogUtils.e("DownloadDBHelper", "Failed:" + e);
                }
            }
        }
        return list;
    }

    /**
     * 根据文件系统状态同步 DownloadTask 真实进度
     * 用于 APP 重启后从 DB 加载时恢复状态
     * 文件系统是状态的唯一真实来源，不依赖 DB 中可能过时的 status
     */
    private void syncFromFileSystem(DownloadTask task) {
        File target = new File(task.savePath);
        File temp = new File(task.savePath + ".temp");

        // target 完整 FINISHED
        if (target.exists() && task.totalBytes > 0 && target.length() == task.totalBytes) {
            task.downloadedBytes = task.totalBytes;
            task.progress = 100.0;
            task.status = DownloadTask.Status.FINISHED;
            return;
        }

        // target 不完整或不存在，检查 temp
        if (temp.exists()) {
            // temp 存在 → PAUSED，用真实大小恢复 progress
            task.downloadedBytes = temp.length();
            task.progress = task.totalBytes > 0 ? DownloadUtils.roundProgress(task.downloadedBytes, task.totalBytes) : 0;
            task.status = DownloadTask.Status.PAUSED;
        } else {
            // temp 不存在 → IDLE（未开始或已完全清理）
            task.downloadedBytes = 0;
            task.progress = 0;
            task.status = DownloadTask.Status.IDLE;
        }
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "task_id=?", new String[]{taskId});
    }
}
