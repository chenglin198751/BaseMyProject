package com.wcl.test.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BigStringDb implements IBigString {

    private static final class InstanceHolder {
        private static final BigStringDb INSTANCE = new BigStringDb();
    }

    public static BigStringDb getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class DBHelperHolder {
        private static final BigDbSQLite INSTANCE = new BigDbSQLite();
    }

    private BigStringDb() {
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
        for (String key : keys) {
            if (TextUtils.isEmpty(key)) {
                return false;
            }
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

        List<String> values = new ArrayList<>(keys.size());
        SQLiteDatabase db = DBHelperHolder.INSTANCE.getReadableDatabase();

        String[] selectionArgs = keys.toArray(new String[0]);
        String selection = TextUtils.join(",", Collections.nCopies(keys.size(), "?"));
        Map<String, String> valueByKey = new HashMap<>(keys.size());

        try (Cursor cursor = db.query(
                BigDbSQLite.TABLE_NAME,
                new String[]{BigDbSQLite.T_KEY, BigDbSQLite.T_VALUE},
                BigDbSQLite.T_KEY + " IN (" + selection + ")",
                selectionArgs,
                null, null, null)) {

            while (cursor.moveToNext()) {
                valueByKey.put(cursor.getString(0), cursor.getString(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String key : keys) {
            values.add(valueByKey.get(key));
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
        public static final int TABLE_VERSION = 2;

        public BigDbSQLite() {
            super(BaseApp.getApp(), "app_common.db", null, TABLE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            final String sql = String.format("CREATE TABLE %s(%s TEXT NOT NULL PRIMARY KEY, %s TEXT)", TABLE_NAME, T_KEY, T_VALUE);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (oldVersion < 2) {
                final String newTableName = TABLE_NAME + "_new";
                db.execSQL("DROP TABLE IF EXISTS " + newTableName);
                db.execSQL(String.format(
                        "CREATE TABLE %s(%s TEXT NOT NULL PRIMARY KEY, %s TEXT)",
                        newTableName, T_KEY, T_VALUE));
                db.execSQL(String.format(
                        "INSERT OR REPLACE INTO %s(%s, %s) SELECT %s, %s FROM %s WHERE %s IS NOT NULL",
                        newTableName, T_KEY, T_VALUE, T_KEY, T_VALUE, TABLE_NAME, T_KEY));
                db.execSQL("DROP TABLE " + TABLE_NAME);
                db.execSQL(String.format("ALTER TABLE %s RENAME TO %s", newTableName, TABLE_NAME));
            }
        }
    }
}
