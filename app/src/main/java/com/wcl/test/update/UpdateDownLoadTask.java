package com.wcl.test.update;

import android.app.Notification;
import android.content.Context;
import android.widget.RemoteViews;

import java.io.File;
import java.io.IOException;

import com.wcl.test.base.BaseApp;
import com.wcl.test.bean.ApkItem;
import com.wcl.test.R;
import com.wcl.test.httpwork.HttpUtils;
import okhttp3.Call;

import com.wcl.test.utils.ApkInstallUtils;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.FileUtils;
import com.wcl.test.widget.ToastUtils;

public class UpdateDownLoadTask {
    private static final int DOWN_NOTIFY_ID = 0;
    private boolean isDownLoading = false;
    private Notification downNotification;
    private android.app.NotificationManager NotificationManager;
    private UpdateDialog mUpdateDialog;

    public UpdateDownLoadTask(UpdateDialog dialog) {
        mUpdateDialog = dialog;
        NotificationManager = (android.app.NotificationManager) BaseApp.getApp().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void start(final String url) {
        isDownLoading = true;
        clearTempFile();
        showNotification(0);

        final HttpUtils.HttpDownloadCallback downloadCallback = new HttpUtils.HttpDownloadCallback() {
            @Override
            public void onSuccess(String filePath) {
                mUpdateDialog.downloadSuccess();
                cancelNotify();
                ApkInstallUtils.openApk(BaseApp.getApp(), filePath);
                isDownLoading = false;
            }

            @Override
            public void onProgress(Call call, long fileTotalSize, long fileDowningSize, float percent) {
                isDownLoading = true;
                showNotification((int) (percent *100));
            }

            @Override
            public void onFailure(IOException e) {
                isDownLoading = false;
                suddenBreadNet();
                clearTempFile();
                ToastUtils.show(R.string.net_error);
            }
        };

        HttpUtils.downloadFile(url, getApkPath(), false, downloadCallback);
    }


    /**
     * 是否正在下载
     */
    public boolean isDownLoading() {
        return isDownLoading;
    }

    /**
     * 下载的通知栏
     */
    public void showNotification(int progress) {
        final String text = "正在下载：" + progress + "%";

        if (downNotification == null) {
            RemoteViews remoteView = new RemoteViews(BaseApp.getApp().getPackageName(), R.layout.notification_progress_layout);
            downNotification = new Notification();
            downNotification.icon = R.drawable.ic_launcher;
            downNotification.contentView = remoteView;
            downNotification.flags = Notification.FLAG_ONGOING_EVENT;
            downNotification.tickerText = text;
            downNotification.contentIntent = null;

            remoteView.setProgressBar(R.id.progressBar1, 100, progress, false);
            remoteView.setTextViewText(R.id.progress, progress + "%");
            NotificationManager.notify(DOWN_NOTIFY_ID, downNotification);
        } else {
            downNotification.contentView.setProgressBar(R.id.progressBar1, 100, progress, false);
            downNotification.contentView.setTextViewText(R.id.progress, progress + "%");
            NotificationManager.notify(DOWN_NOTIFY_ID, downNotification);
        }
    }


    /**
     * 取消通知栏
     */
    public void cancelNotify() {
        NotificationManager.cancel(DOWN_NOTIFY_ID);
    }

    /**
     * 突然断网，取消下载进度
     */
    public void suddenBreadNet() {
        BaseUtils.getHandler().post(new Runnable() {
            @Override
            public void run() {
                cancelNotify();
                mUpdateDialog.suddenBreadNet();
            }
        });
    }

    private void clearTempFile() {
        File myFile = new File(getApkPath());
        myFile.delete();
    }


    public static boolean apkExist(Context context, String versionName) {
        ApkItem apkItem = BaseUtils.getApkInfo(context, getApkPath());

        if (apkItem == null) {
            return false;
        }

        if (versionName.equals(apkItem.appVersion)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getApkPath() {
        return FileUtils.getExternalPath() + BaseUtils.getPackageName() + ".apk";
    }
}
