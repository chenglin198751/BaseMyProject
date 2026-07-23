package com.wcl.test.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.wcl.test.base.BaseActivity;
import com.wcl.test.bean.ApkItem;
import com.wcl.test.widget.ToastUtils;

import java.io.File;

public class ApkInstaller {
    private static final String DATA_TYPE_APK = "application/vnd.android.package-archive";
    private static final String FILE_PROVIDER_NAME = ".custom.file_provider";

    /**
     * 安装一个APK包，此方法不需要获取安装权限，原理是调用系统安装包管理打开apk。建议优先使用此方法。
     */
    public static void installApk(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)) {
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            Toast.makeText(context, "安装失败，找不到apk文件", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = null;
        if (file.getName().toLowerCase().endsWith("apk")) {
            intent = generateCommonIntent(context, filePath, DATA_TYPE_APK);
        }
        context.startActivity(intent);
    }

    /**
     * 根据包名打开别的应用
     */
    public static void startApp(BaseActivity context, String packageName) {
        boolean isInstall = false;
        try {
            if (isInstalledApp(context, packageName)) {
                isInstall = true;
                Intent LaunchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
                context.startActivity(LaunchIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!isInstall) {
            ToastUtils.show("你手机没安装此应用");
        }
    }

    /**
     * 是否安装了此应用
     */
    public static boolean isInstalledApp(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return false;
        }

        PackageManager pm = context.getApplicationContext().getPackageManager();
        try {
            pm.getApplicationInfo(packageName, 0);
            return true;
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return false;
    }

    /**
     * 从 apk 文件路径中解析 APK 基本信息
     *
     * @param context 上下文（必须是 Application Context）
     * @param path    apk 文件绝对路径
     * @return ApkItem，解析失败返回 null
     */
    public static ApkItem getApkInfo(Context context, String path) {
        if (context == null || TextUtils.isEmpty(path)) {
            return null;
        }

        File apkFile = new File(path);
        if (!apkFile.exists() || !apkFile.isFile()) {
            return null;
        }

        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo;

        try {
            packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        } catch (Throwable t) {
            return null;
        }

        if (packageInfo == null || packageInfo.applicationInfo == null) {
            return null;
        }

        ApkItem item = new ApkItem();
        item.appSize = apkFile.length();
        item.appVersion = packageInfo.versionName;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            item.versionCode = packageInfo.getLongVersionCode();
        } else {
            item.versionCode = packageInfo.versionCode;
        }

        ApplicationInfo appInfo = packageInfo.applicationInfo;
        appInfo.sourceDir = path;
        appInfo.publicSourceDir = path;

        try {
            item.appName = appInfo.loadLabel(pm).toString().trim();
            item.image = appInfo.loadIcon(pm);
        } catch (Throwable t) {
            item.appName = "";
            item.image = null;
        }

        item.packageName = packageInfo.packageName;
        return item;
    }

    private static Intent generateCommonIntent(Context context, String filePath, String dataType) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        File file = new File(filePath);
        Uri uri = getUri(context, intent, file);
        intent.setDataAndType(uri, dataType);
        return intent;
    }

    private static Uri getUri(Context context, Intent intent, File file) {
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


}
