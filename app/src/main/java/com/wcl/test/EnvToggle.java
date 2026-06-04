package com.wcl.test;


import com.wcl.test.storage.ToggleSettings;

public class EnvToggle {
    private static final boolean isDebug = BuildConfig.DEBUG || ToggleSettings.getDebugEnable();
    private static final boolean isLog = BuildConfig.LOG || ToggleSettings.getLogEnable();

    /**
     * 是否打印日志
     */
    public static boolean isLog() {
        return isLog;
    }

    /**
     * 是否debug模式
     */
    public static boolean isDebug() {
        return isDebug;
    }
}
