package com.wcl.test.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.text.TextUtils;

import com.wcl.test.base.BaseApp;

public class DeviceUtils {
    private static String androidId = null;
    private static String m2 = null;

    public static String getDeviceId() {
        Context context = BaseApp.getApp();
        try {
            if (TextUtils.isEmpty(m2)) {
                m2 = MD5Util.md5LowerCase(getWifiMac(context) + getAndroidId(context));
            }
            return m2;
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
        return "";
    }

    private static String getAndroidId(Context context) {
        try {
            if (TextUtils.isEmpty(androidId)) {
                androidId = Settings.System.getString(context.getContentResolver(), "android_id");
            }
            return androidId;
        } catch (Exception e) {
        }
        return "";
    }

    private static String getWifiMac(Context context) {
        String macAddr = null;
        if (context != null) {
            try {
                WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                if (wifiMgr != null) {
                    WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
                    if (wifiInfo != null) {
                        macAddr = wifiInfo.getMacAddress();
                    }
                }
            } catch (Error e) {
            }
        }
        return macAddr;
    }

}
