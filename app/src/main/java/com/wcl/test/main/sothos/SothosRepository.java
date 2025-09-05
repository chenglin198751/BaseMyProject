package com.wcl.test.main.sothos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SothosRepository {
    private final SothosDbHelper dbHelper;
    private static final String TABLE_NAME = "sothos_events";

    public SothosRepository(Context context) {
        dbHelper = new SothosDbHelper(context);
    }

    /**
     * 插入单条埋点事件
     */
    public void insertEvent(String data) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("data", data);
        values.put("ts", System.currentTimeMillis());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    /**
     * 获取所有埋点事件
     */
    public List<String> getAllEvents() {
        List<String> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, new String[]{"id", "data"}, null, null, null, null, null);
        while (cursor.moveToNext()) {
            list.add(cursor.getString(1));
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * 删除单条埋点事件
     */
    public void deleteEvent(Integer id) {
        List<Integer> ids = new ArrayList<>();
        ids.add(id);
        deleteEvents(ids);
    }

    /**
     * 删除多条埋点事件
     */
    public void deleteEvents(List<Integer> ids) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        for (int id : ids) {
            db.delete(TABLE_NAME, "id=?", new String[]{String.valueOf(id)});
        }
        db.close();
    }

    private static class SothosDbHelper extends SQLiteOpenHelper {
        private static final String DB_NAME = "sothos_events.db";
        private static final int DB_VERSION = 1;

        public SothosDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String createTableSql = String.format(
                    "CREATE TABLE %s (" +
                            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "data TEXT," +
                            "extra1 TEXT DEFAULT ''," +
                            "extra2 TEXT DEFAULT ''," +
                            "extra3 TEXT DEFAULT ''," +
                            "ts INTEGER)",
                    TABLE_NAME
            );
            db.execSQL(createTableSql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String dropSql = String.format("DROP TABLE IF EXISTS %s", TABLE_NAME);
            db.execSQL(dropSql);
            onCreate(db);
        }
    }
}
