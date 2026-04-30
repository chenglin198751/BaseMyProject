package com.wcl.test.base;

import android.content.Intent;
import android.os.Bundle;

import com.wcl.test.utils.AppUtils;

/**
 * ---- 2026-01-13 目前不用，所以类权限设置为包内可见----
 * 使用广播实现的跨进程通信方案，限定在app内跨进程
 */
class EventBusIpc {

    /**
     * 发送跨进程广播
     */
    public static void post(String event, Bundle data) {
        if (data == null) {
            data = new Bundle();
        }

        Intent intent = new Intent(EventBusIpcHelper.ACTION_BASE_BROADCAST);
        intent.setPackage(AppUtils.getPackageName());
        intent.putExtra("action", event);
        intent.putExtra("bundle", data);
        BaseApp.getApp().sendBroadcast(intent);
    }
}
