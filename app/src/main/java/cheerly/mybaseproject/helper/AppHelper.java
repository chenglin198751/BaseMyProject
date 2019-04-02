package cheerly.mybaseproject.helper;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import cheerly.mybaseproject.base.BaseApp;
import cheerly.mybaseproject.httpwork.HttpUtils;
import cheerly.mybaseproject.utils.BaseUtils;

/**
 * Created by chenglin on 2017-5-24.
 */

public class AppHelper {
    private final static String PROCESS_NAME = BaseUtils.getPackageName();

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
}
