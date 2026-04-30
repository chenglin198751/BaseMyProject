//package com.wcl.test.base;
//
//import android.app.Activity;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//
//import androidx.core.content.ContextCompat;
//
//import com.wcl.test.utils.AppUtils;
//
///**
// * ---- 2026-01-13 目前不用，所以类权限设置为包内可见----
// * 使用广播实现的跨进程通信方案，限定在app内跨进程
// */
//public class EventBusIpc {
//    private static final String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
//    private BroadcastReceiver mBroadcastReceiver;
//
//    /**
//     * 发送跨进程事件
//     */
//    public static void post(String event, Bundle data) {
//        if (data == null) {
//            data = new Bundle();
//        }
//
//        Intent intent = new Intent(ACTION_BASE_BROADCAST);
//        intent.setPackage(AppUtils.getPackageName());
//        intent.putExtra("action", event);
//        intent.putExtra("bundle", data);
//        BaseApp.getApp().sendBroadcast(intent);
//    }
//
//    private void registerBroadcastReceiver(Activity activity) {
//        if (mBroadcastReceiver != null) return;
//        mBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                if (ACTION_BASE_BROADCAST.equals(intent.getAction())) {
//                    String childAction = intent.getStringExtra("action");
//                    onBroadcastReceiver(childAction, intent.getBundleExtra("bundle"));
//                }
//            }
//        };
//        IntentFilter filter = new IntentFilter(ACTION_BASE_BROADCAST);
//        ContextCompat.registerReceiver(activity, mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
//    }
//
//    private void unregisterBroadcastReceiver(Activity activity) {
//        if (mBroadcastReceiver != null) {
//            activity.unregisterReceiver(mBroadcastReceiver);
//            mBroadcastReceiver = null;
//        }
//    }
//
//
//}
