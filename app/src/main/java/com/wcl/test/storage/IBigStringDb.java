package com.wcl.test.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IBigStringDb implements IBigString {

    private static final class InstanceHolder {
        private static final IBigStringDb INSTANCE = new IBigStringDb();
    }

    public static IBigStringDb getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class DBHelperHolder {
        private static final BigDbSQLite INSTANCE = new BigDbSQLite();
    }

    private IBigStringDb() {
    }

    @Override
    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        SQLiteDatabase db = DBHelperHolder.INSTANCE.getReadableDatabase();

        try (Cursor cursor = db.query(BigDbSQLite.TABLE_NAME, new String[]{BigDbSQLite.T_KEY}, null, null, null, null, null)) {
            while (cursor.moveToNext()) {
                keys.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return keys;
    }

    @Override
    public boolean put(String key, String value) {
        return putValues(Collections.singletonList(key), Collections.singletonList(value));
    }

    @Override
    public String get(String key) {
        List<String> values = getValues(Collections.singletonList(key));
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }

    @Override
    public boolean putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.isEmpty() || values == null || values.isEmpty() || keys.size() != values.size()) {
            return false;
        }

        SQLiteDatabase db = DBHelperHolder.INSTANCE.getWritableDatabase();
        db.beginTransaction();
        int count = 0;

        try {
            ContentValues contentValues = new ContentValues();
            for (int i = 0; i < keys.size(); i++) {
                contentValues.clear();
                contentValues.put(BigDbSQLite.T_KEY, keys.get(i));
                contentValues.put(BigDbSQLite.T_VALUE, values.get(i));
                long rowId = db.replace(BigDbSQLite.TABLE_NAME, null, contentValues);
                if (rowId > 0) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return count == keys.size();
    }

    @Override
    public List<String> getValues(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return null;
        }

        List<String> values = new ArrayList<>();
        SQLiteDatabase db = DBHelperHolder.INSTANCE.getReadableDatabase();

        String[] selectionArgs = keys.toArray(new String[0]);
        String selection = TextUtils.join(",", Collections.nCopies(keys.size(), "?"));

        try (Cursor cursor = db.query(
                BigDbSQLite.TABLE_NAME,
                new String[]{BigDbSQLite.T_VALUE},
                BigDbSQLite.T_KEY + " IN (" + selection + ")",
                selectionArgs,
                null, null, null)) {

            while (cursor.moveToNext()) {
                values.add(cursor.getString(0));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return values;
    }

    @Override
    public boolean remove(String key) {
        return remove(Collections.singletonList(key));
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = DBHelperHolder.INSTANCE.getWritableDatabase();
        db.beginTransaction();
        int count = 0;

        try {
            for (String key : keys) {
                int deletedRows = db.delete(
                        BigDbSQLite.TABLE_NAME,
                        BigDbSQLite.T_KEY + " = ?",
                        new String[]{key});
                if (deletedRows > 0) {
                    count++;
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }

        return count == keys.size();
    }

    private static class BigDbSQLite extends SQLiteOpenHelper {
        public static final String TABLE_NAME = "common_app_setting";
        public static final String T_KEY = "t_key";
        public static final String T_VALUE = "t_value";
        public static final int TABLE_VERSION = 1;

        public BigDbSQLite() {
            super(BaseApp.getApp(), "app_common.db", null, TABLE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String sql = String.format("CREATE TABLE %s(%s TEXT PRIMARY KEY, %s TEXT)", TABLE_NAME, T_KEY, T_VALUE);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // 数据库升级逻辑（可根据需要实现）
        }
    }
}
