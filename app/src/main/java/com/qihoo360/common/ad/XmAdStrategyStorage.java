package com.qihoo360.common.ad;

import android.content.Context;
import android.content.SharedPreferences;

import com.qihoo.utils.ContextUtils;

class XmAdStrategyStorage {
    private static final String PREF_NAME = "xm_user_app_ad_strategy";
    private static XmAdStrategyStorage instance;
    private final SharedPreferences sharedPreferences;

    private XmAdStrategyStorage() {
        sharedPreferences = ContextUtils.getHostAppContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static XmAdStrategyStorage get() {
        if (instance == null) {
            synchronized (XmAdStrategyStorage.class) {
                if (instance == null) {
                    instance = new XmAdStrategyStorage();
                }
            }
        }
        return instance;
    }

    // 存储字符串数据
    public void saveString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // 获取字符串数据
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    // 存储布尔数据
    public void saveBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    // 获取布尔数据
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    // 存储整数数据
    public void saveInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    // 获取整数数据
    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    // 存储长整数数据
    public void saveLong(String key, long value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    // 获取长整数数据
    public long getLong(String key, long defaultValue) {
        return sharedPreferences.getLong(key, defaultValue);
    }

    // 存储浮点数据
    public void saveFloat(String key, float value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    // 获取浮点数据
    public float getFloat(String key, float defaultValue) {
        return sharedPreferences.getFloat(key, defaultValue);
    }

    // 移除某个键值对
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

    // 清除所有数据
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}