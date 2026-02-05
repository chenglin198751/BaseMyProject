package com.wcl.test.update;

import android.app.Notification;
import android.content.Context;

import com.wcl.test.R;
import com.wcl.test.base.BaseApp;
import com.wcl.test.bean.ApkItem;
import com.wcl.test.http.HttpUtils;
import com.wcl.test.utils.ApkInstaller;
import com.wcl.test.utils.AppUtils;
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
        showNotification(0);

        HttpUtils.download(url, new HttpUtils.DownloadCallback() {
            @Override
            public void onProgress(long total, long current, float percent) {
                isDownLoading = true;
                showNotification((int) (percent * 100));
            }

            @Override
            public void onFinished(boolean success, String filePath, String error) {
                if (success) {
                    mUpdateDialog.downloadSuccess();
                    cancelNotify();
                    ApkInstaller.installApk(BaseApp.getApp(), filePath);
                    isDownLoading = false;
                } else {
                    isDownLoading = false;
                    suddenBreadNet();
                    ToastUtils.show(R.string.net_error);
                }
            }
        });
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

//        if (downNotification == null) {
//            RemoteViews remoteView = new RemoteViews(BaseApp.getApp().getPackageName(), R.layout.notification_progress_layout);
//            downNotification = new Notification();
//            downNotification.icon = R.drawable.ic_launcher;
//            downNotification.contentView = remoteView;
//            downNotification.flags = Notification.FLAG_ONGOING_EVENT;
//            downNotification.tickerText = text;
//            downNotification.contentIntent = null;
//
//            remoteView.setProgressBar(R.id.progressBar1, 100, progress, false);
//            remoteView.setTextViewText(R.id.progress, progress + "%");
//            NotificationManager.notify(DOWN_NOTIFY_ID, downNotification);
//        } else {
//            downNotification.contentView.setProgressBar(R.id.progressBar1, 100, progress, false);
//            downNotification.contentView.setTextViewText(R.id.progress, progress + "%");
//            NotificationManager.notify(DOWN_NOTIFY_ID, downNotification);
//        }
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
        AppUtils.getUiHandler().post(new Runnable() {
            @Override
            public void run() {
                cancelNotify();
                mUpdateDialog.suddenBreadNet();
            }
        });
    }

    public static boolean apkExist(Context context, String versionName, String apk_path) {
        ApkItem apkItem = ApkInstaller.getApkInfo(context, apk_path);

        if (apkItem == null) {
            return false;
        }

        if (versionName.equals(apkItem.appVersion)) {
            return true;
        } else {
            return false;
        }
    }
}
