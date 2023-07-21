package com.wcl.test.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;

public class ApkInstaller {
    private static final String DATA_TYPE_APK = "application/vnd.android.package-archive";
    private static final String FILE_PROVIDER_NAME = ".custom.file_provider";

    /**
     * 安装一个APK包，此方法不需要获取安装权限，原理是调用系统安装包管理打开apk。建议优先使用此方法。
     */
    public static void installApk(Context context, String filePath) {
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
            uri = FileProvider.getUriForFile(context, context.getPackageName() + FILE_PROVIDER_NAME, file);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }


}
