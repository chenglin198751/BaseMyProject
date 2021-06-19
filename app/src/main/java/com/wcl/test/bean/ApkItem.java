package com.wcl.test.bean;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class ApkItem {
    public String appName, appVersion, packageName;
    public Drawable image;
    public boolean isInstalled = false;
    public String path;
    public String ID;
    public String appDesc = "";
    public long appSize = 0;
    public int versionCode;
    public PackageInfo packageInfo;
}
