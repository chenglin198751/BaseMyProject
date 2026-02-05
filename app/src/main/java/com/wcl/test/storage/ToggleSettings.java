package com.wcl.test.storage;

import com.tencent.mmkv.MMKV;

public class ToggleSettings {

    private static final MMKV kv = MMKV.defaultMMKV();
    private static final String KEY_LOG_TOGGLE = "KEY_LOG_TOGGLE";
    private static final String KEY_DEBUG_TOGGLE = "KEY_DEBUG_TOGGLE";

    public static void setLogEnable(boolean value) {
        kv.encode(KEY_LOG_TOGGLE, value);
    }

    public static boolean getLogEnable() {
        return kv.decodeBool(KEY_LOG_TOGGLE, false);
    }

    public static void setDebugEnable(boolean value) {
        kv.encode(KEY_DEBUG_TOGGLE, value);
    }

    public static boolean getDebugEnable() {
        return kv.decodeBool(KEY_DEBUG_TOGGLE, false);
    }
}
