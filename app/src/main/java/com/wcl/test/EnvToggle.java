package com.wcl.test;


import com.wcl.test.preferences.PreferAppSettings;

public class EnvToggle {
    public static final boolean isLog = PreferAppSettings.getLogEnable();
    public static final boolean isDebug = PreferAppSettings.getDebugEnable();
}
