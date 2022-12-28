package com.wcl.test.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class ApkInstallUtils {
    private static final String DATA_TYPE_APK = "application/vnd.android.package-archive";

    public static void openApk(Context context, String filePath) {
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
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".custom.file_provider", file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 安装一个APK包
     */
    public static void installApk(Context context, String path) {
        try {
            installApk2(context, path);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void installApk2(Context context, String path) {
        Activity activity = (Activity) context;
        if (activity == null) {
            return;
        }
        File file = new File(path);
        if (file.exists()) {
            Intent installApkIntent = new Intent();
            installApkIntent.setAction(Intent.ACTION_VIEW);
            installApkIntent.addCategory(Intent.CATEGORY_DEFAULT);
            installApkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //适配8.0需要有权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                boolean hasInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (hasInstallPermission) {
                    //安装应用
                    installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    context.startActivity(installApkIntent);
                } else {
                    //跳转至“安装未知应用”权限界面，引导用户开启权限
                    Uri selfPackageUri = Uri.parse("package:" + context.getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, selfPackageUri);
                    activity.startActivityForResult(intent, 10098);
                }
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    installApkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    installApkIntent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                }
                context.startActivity(installApkIntent);
            }
        }
    }

}
