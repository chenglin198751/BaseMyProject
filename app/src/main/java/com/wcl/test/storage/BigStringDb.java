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

public class BigStringDb implements BigStringBase {

    private BigStringDb() {
    }

    private static final class InstanceHolder {
        static final BigStringDb INSTANCE = new BigStringDb();
    }

    public static BigStringDb getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();
        BigDbSQLite dbHelper = new BigDbSQLite();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = new String[]{BigDbSQLite.T_KEY};
        Cursor cursor = db.query(BigDbSQLite.TABLE_NAME, columns, null, null, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                keys.add(cursor.getString(0));
            }
            cursor.close();
        }

        db.close();
        dbHelper.close();
        return keys;
    }

    @Override
    public boolean put(String key, String value) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        List<String> values = new ArrayList<>();
        values.add(value);
        return putValues(keys, values);
    }

    @Override
    public String get(String key) {
        List<String> keys = new ArrayList<>();
        keys.add(key);

        List<String> values = getValues(keys);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }


    @Override
    public boolean putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.size() == 0) {
            return false;
        } else if (values == null || values.size() == 0) {
            return false;
        } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("keys.size() != values.size()");
        }

        BigDbSQLite dbHelper = new BigDbSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        for (int i = 0; i < keys.size(); i++) {
            ContentValues contents = new ContentValues();
            contents.put(BigDbSQLite.T_KEY, keys.get(i));
            contents.put(BigDbSQLite.T_VALUE, values.get(i));
            long row_id = db.replace(BigDbSQLite.TABLE_NAME, null, contents);
            if (row_id > 0) {
                count++;
            }
        }

        db.close();
        dbHelper.close();
        return count == keys.size();
    }

    @Override
    public List<String> getValues(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }

        List<String> values = new ArrayList<>();
        BigDbSQLite dbHelper = new BigDbSQLite();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = new String[keys.size()];
        keys.toArray(selectionArgs);

        String selection = TextUtils.join(",", Collections.nCopies(keys.size(), "?"));
        String sql_query = String.format("SELECT %s FROM %s WHERE %s in (%s)", BigDbSQLite.T_VALUE, BigDbSQLite.TABLE_NAME, BigDbSQLite.T_KEY, selection);

        Cursor cursor = db.rawQuery(sql_query, selectionArgs);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                values.add(cursor.getString(0));
            }
            cursor.close();
        }

        db.close();
        dbHelper.close();
        return values;
    }

    @Override
    public boolean remove(String key) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        return remove(keys);
    }

    @Override
    public boolean remove(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return false;
        }

        BigDbSQLite dbHelper = new BigDbSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        for (int i = 0; i < keys.size(); i++) {
            String selection = BigDbSQLite.T_KEY + " = ?";
            int deletedRows = db.delete(BigDbSQLite.TABLE_NAME, selection, new String[]{keys.get(i)});
            if (deletedRows > 0) {
                count++;
            }
        }

        db.close();
        dbHelper.close();
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
            final String sql = String.format("create table %s(%s primary key,%s)", TABLE_NAME, T_KEY, T_VALUE);
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }

}


