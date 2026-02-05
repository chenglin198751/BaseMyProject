package com.wcl.test.storage;

import com.tencent.mmkv.MMKV;

public class ToggleSettings {

    private static final String KEY_LOG_TOGGLE = "KEY_LOG_TOGGLE";
    private static final String KEY_DEBUG_TOGGLE = "KEY_DEBUG_TOGGLE";

    public static void setLogEnable(boolean value) {
        MMKV.defaultMMKV().encode(KEY_LOG_TOGGLE, value);
    }

    public static boolean getLogEnable() {
        return MMKV.defaultMMKV().decodeBool(KEY_LOG_TOGGLE, false);
    }

    public static void setDebugEnable(boolean value) {
        MMKV.defaultMMKV().encode(KEY_DEBUG_TOGGLE, value);
    }

    public static boolean getDebugEnable() {
        return MMKV.defaultMMKV().decodeBool(KEY_DEBUG_TOGGLE, false);
    }
}
