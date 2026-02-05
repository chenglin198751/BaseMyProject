package com.wcl.test.storage;

import com.tencent.mmkv.MMKV;

public class UserManager {

    private static final MMKV kv;
    private static final String uid = "uid";

    static {
        kv = MMKV.mmkvWithID("app_user_manager");
    }

    public static void clear() {
        kv.clearAll();
    }

    public static void setUid(String lastLogin) {
        kv.encode(uid, lastLogin);
    }

    public static String getUid() {
        return kv.decodeString(uid, "");
    }

}
