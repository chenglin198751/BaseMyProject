package com.wcl.test.preferences;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.wcl.test.base.BaseApp;

public class CommonSQLite extends SQLiteOpenHelper {
    public static final String TABLE_NAME = "app_setting";
    public static final String T_KEY = "t_key";
    public static final String T_VALUE = "t_value";
    public static final int TABLE_VERSION = 1;

    public CommonSQLite() {
        super(BaseApp.getApp(), "app_common.db", null, TABLE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table TABLE_NAME" +
                "(T_KEY TEXT primary key," +
                "T_VALUE TEXT," +
                "column1 TEXT," +
                "column2 TEXT," +
                "column3 TEXT," +
                "column4 TEXT," +
                "column5 TEXT)";

        sql = sql.replace("TABLE_NAME", TABLE_NAME)
                .replace("T_KEY", T_KEY)
                .replace("T_VALUE", T_VALUE);

        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
