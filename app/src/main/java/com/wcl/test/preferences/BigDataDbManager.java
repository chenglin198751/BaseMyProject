package com.wcl.test.preferences;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.wcl.test.utils.AppLogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BigDataDbManager {
    private static final String TAG = "KvDbManager";

    public static boolean put(String key, String value) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CommonSQLite.T_KEY, key);
        values.put(CommonSQLite.T_VALUE, value);

        // 先查询T_KEY列是否有key值，如果有就update，没有就insert
        final String sql_query = "select * from " + CommonSQLite.TABLE_NAME + " where " + CommonSQLite.T_KEY + " = ?";
        Cursor cursor = db.rawQuery(sql_query, new String[]{key});
        long row_id = -1;

        if (cursor != null) {
            if (cursor.getCount() > 0) {
                AppLogUtils.v(TAG, "update");
                row_id = db.update(CommonSQLite.TABLE_NAME, values, CommonSQLite.T_KEY + " = ?", new String[]{key});
            } else {
                AppLogUtils.v(TAG, "insert");
                row_id = db.insert(CommonSQLite.TABLE_NAME, null, values);
            }
            cursor.close();
        }

        db.close();
        dbHelper.close();
        return row_id > 0;
    }

    public static String get(String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }

        String value = null;
        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = new String[]{CommonSQLite.T_VALUE};
        String selection = CommonSQLite.T_KEY + " = ?";
        Cursor cursor = db.query(CommonSQLite.TABLE_NAME, columns, selection, new String[]{key}, null, null, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                value = cursor.getString(0);

            }
            cursor.close();
        }

        db.close();
        dbHelper.close();
        return value;
    }

    public static boolean putValues(List<String> keys, List<String> values) {
        if (keys == null || keys.size() == 0) {
            return false;
        } else if (values == null || values.size() == 0) {
            return false;
        } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("keys.size() != values.size()");
        }

        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        for (int i = 0; i < keys.size(); i++) {
            ContentValues contents = new ContentValues();
            contents.put(CommonSQLite.T_KEY, keys.get(i));
            contents.put(CommonSQLite.T_VALUE, values.get(i));
            long row_id = db.replace(CommonSQLite.TABLE_NAME, null, contents);
            if (row_id > 0) {
                count++;
            }
        }


        db.close();
        dbHelper.close();
        return count == keys.size();
    }

    public static List<String> getValues(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return null;
        }

        List<String> values = new ArrayList<>();
        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] selectionArgs = new String[keys.size()];
        keys.toArray(selectionArgs);

        String selection = TextUtils.join(",", Collections.nCopies(keys.size(), "?"));
        String sql_query = "SELECT " + CommonSQLite.T_VALUE + " FROM " + CommonSQLite.TABLE_NAME +
                " WHERE " + CommonSQLite.T_KEY + " in (" + selection + ")";

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

    public static boolean remove(String key) {
        if (TextUtils.isEmpty(key)) {
            return false;
        }

        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = CommonSQLite.T_KEY + " = ?";
        int deletedRows = db.delete(CommonSQLite.TABLE_NAME, selection, new String[]{key});

        db.close();
        dbHelper.close();
        return deletedRows > 0;
    }

    public static boolean remove(List<String> keys) {
        if (keys == null || keys.size() == 0) {
            return false;
        }

        CommonSQLite dbHelper = new CommonSQLite();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int count = 0;
        for (int i = 0; i < keys.size(); i++) {
            String selection = CommonSQLite.T_KEY + " = ?";
            int deletedRows = db.delete(CommonSQLite.TABLE_NAME, selection, new String[]{keys.get(i)});
            if (deletedRows > 0) {
                count++;
            }
        }

        db.close();
        dbHelper.close();
        return count == keys.size();
    }

}
