package com.wcl.test.storage;

import com.tencent.mmkv.MMKV;

public class PreferAppSettings {

    private static final MMKV kv = MMKV.defaultMMKV();
    private static final String KEY_ANDROID_ID = "KEY_ANDROID_ID";
    private static final String KEY_UPDATE_TIME = "KEY_UPDATE_TIMES";

    public static String getAndroidId() {
        return kv.decodeString(KEY_ANDROID_ID, "");
    }

    public static void setAndroidId(String androidId) {
        kv.encode(KEY_ANDROID_ID, androidId);
    }

    public static Long getUpdateTime() {
        return kv.decodeLong(KEY_UPDATE_TIME, 0);
    }

    public static void setUpdateTime(long value) {
        kv.encode(KEY_UPDATE_TIME, value);
    }
}
