package com.wcl.test;


import com.wcl.test.preferences.ToggleSettings;
import com.wcl.test.utils.AppBaseUtils;

public class EnvToggle {
    private static final boolean isDebug = ToggleSettings.getDebugEnable();
    private static final boolean isLog = AppBaseUtils.isDebuggable() || ToggleSettings.getLogEnable();

    public static boolean isLog() {
        return isLog;
    }

    public static boolean isDebug() {
        return isDebug;
    }
}
