package com.wcl.test.base;

import android.content.Intent;
import android.os.Bundle;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BaseAction {

    /**
     * 通用的发送广播，可以在任意位置发送
     */
    public static void sendBroadcast(String action, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }

        Intent intent = new Intent(System.ACTION_BASE_BROADCAST);
        intent.putExtra("action", action);
        intent.putExtra("bundle", bundle);
        LocalBroadcastManager.getInstance(BaseApp.getApp()).sendBroadcast(intent);
    }

    public interface System {
        /**
         * 系统基础广播
         */
        String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
        /**
         * 根据开关onKeepSingleActivity()：当前Activity无论打开多少，只保留最后打开的一个
         */
        String ACTION_KEEP_SINGLE_ACTIVITY = "ACTION_KEEP_SINGLE_ACTIVITY";
        /**
         * 关闭别的Activity，只保留MainActivity不关闭
         */
        String ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY = "ACTION_KEEP_MAIN_AND_CLOSE_ACTIVITY";
    }

    public interface Keys {
        String ACTIVITY_NAME = "activity_name";
    }

    public interface CommonAction {
        String ACTION_TEST = "ACTION_TEST";
    }

}
