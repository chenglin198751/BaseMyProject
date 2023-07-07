package com.wcl.test.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BigDataDbManager {
    private static final String TAG = "BigDataDbManager";

    public static boolean put(String key, String value) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        List<String> values = new ArrayList<>();
        values.add(value);
        return putValues(keys, values);
    }

    public static String get(String key) {
        List<String> keys = new ArrayList<>();
        keys.add(key);

        List<String> values = getValues(keys);
        if (values != null && values.size() > 0) {
            return values.get(0);
        } else {
            return null;
        }
    }

    public static boolean remove(String key) {
        List<String> keys = new ArrayList<>();
        keys.add(key);
        return remove(keys);
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
            // db.replace只针对CommonSQLite.T_KEY是主键时有效
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
