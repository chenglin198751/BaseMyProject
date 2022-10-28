package com.wcl.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wcl.test.utils.AppLogUtils;


/**
 * 此 ContentProvider 仅仅用于巧用 onCreate() 来实现SDK产品的初始化。
 * CustomInitProvider的onCreate()方法，晚于application的attachBaseContext()方法，早于application的onCreate()方法。
 */
public class CustomInitProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        AppLogUtils.v("tag_88", "CustomInitProvider onCreate = " + getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
