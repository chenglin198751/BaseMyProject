package com.wcl.test.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.wcl.test.base.BaseApp;

import java.util.ArrayList;
import java.util.List;

class DownloadDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "okhttp3_download.db";
    private static final int VERSION = 1;
    private static final String TABLE_NAME = "download_task";
    private static final Gson gson = new Gson();

    public DownloadDBHelper() {
        super(BaseApp.getApp(), DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                " (`key` TEXT PRIMARY KEY, value TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public void saveTask(DownloadTask task) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("key", task.taskId);
        cv.put("value", gson.toJson(task));
        db.insertWithOnConflict(TABLE_NAME, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public DownloadTask loadTask(String taskId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{"value"}, "key=?", new String[]{taskId}, null, null, null);
        if (c != null && c.moveToFirst()) {
            String json = c.getString(0);
            c.close();
            return gson.fromJson(json, DownloadTask.class);
        }
        return null;
    }

    public List<DownloadTask> loadAllTasks() {
        List<DownloadTask> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{"value"}, null, null, null, null, null);
        if (c != null) {
            while (c.moveToNext()) {
                String json = c.getString(0);
                list.add(gson.fromJson(json, DownloadTask.class));
            }
            c.close();
        }
        return list;
    }

    public void deleteTask(String taskId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "key=?", new String[]{taskId});
    }
}
