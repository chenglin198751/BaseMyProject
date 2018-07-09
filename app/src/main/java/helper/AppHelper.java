package helper;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import base.BaseApp;
import httpwork.HttpExecutor;

/**
 * Created by chenglin on 2017-5-24.
 */

public class AppHelper {
    private final static String PROCESS_NAME = "cheerly.mybaseproject";

    /**
     * 判断是不是UI主进程，因为有些东西只能在UI主进程初始化
     */
    public static boolean isAppMainProcess() {
        try {
            int pid = android.os.Process.myPid();
            String process = getAppNameByPID(BaseApp.getApp(), pid);
            if (TextUtils.isEmpty(process)) {
                return true;
            } else if (PROCESS_NAME.equalsIgnoreCase(process)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    /**
     * 根据Pid得到进程名
     */
    public static String getAppNameByPID(Context context, int pid) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (android.app.ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return "";
    }

    /**
     * 自定义Picasso的Downloader，用的是Picasso的作者JakeWharton写的用OkHttp3作为下载器的Downloader.
     * 下载路径用的是OkHttp3设置的cache路径。Picasso内部没有实现硬盘缓存，用的是下载器自身带的缓存策略。
     */
    public static void initPicasso() {
        Picasso picasso = new Picasso.Builder(BaseApp.getApp())
                .downloader(new OkHttp3Downloader(HttpExecutor.client))
                .build();
        Picasso.setSingletonInstance(picasso);
    }
}
