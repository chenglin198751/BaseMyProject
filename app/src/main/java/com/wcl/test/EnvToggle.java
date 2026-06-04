package com.wcl.test;


import com.wcl.test.storage.ToggleSettings;

public class EnvToggle {
    private static final boolean isDebug = BuildConfig.DEBUG || ToggleSettings.getDebugEnable();
    private static final boolean isLog = BuildConfig.LOG || ToggleSettings.getLogEnable();

    public static boolean isLog() {
        return isLog;
    }

    public static boolean isDebug() {
        return isDebug;
    }
}
