package com.wcl.test.storage;

import com.tencent.mmkv.MMKV;

public class PreferAppSettings {

    private static final MMKV kv = MMKV.defaultMMKV();
    private static final String update_dialog_times = "update_dialog_times";

    public static Long getUpdateDialogTimes() {
        return kv.decodeLong(update_dialog_times, 0);
    }

    public static void setUpdateDialogTimes(long value) {
        kv.encode(update_dialog_times, value);
    }
}
