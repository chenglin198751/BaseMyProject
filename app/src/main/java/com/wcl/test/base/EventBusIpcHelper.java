package com.wcl.test.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import com.wcl.test.utils.AppUtils;

/**
 * ---- 2026-01-13 目前不用，所以类权限设置为包内可见----
 * 使用广播实现的跨进程通信方案，限定在app内跨进程
 * 使用方法：在BaseActivity的onCreate()中写：new EventBusIpcHelper().addObserver(this);
 */
class EventBusIpcHelper {
    static final String ACTION_BASE_BROADCAST = "ACTION_BASE_BROADCAST";
    private BroadcastReceiver mBroadcastReceiver;
    private BaseActivity mActivity;

    void addObserver(BaseActivity activity) {
        this.mActivity = activity;
        activity.getLifecycle().addObserver(new MyLifecycleObserver());
    }

    private void registerBroadcastReceiver() {
        if (mBroadcastReceiver != null) return;
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_BASE_BROADCAST.equals(intent.getAction())) {
                    String childAction = intent.getStringExtra("action");
                    mActivity.onEvent(childAction, intent.getBundleExtra("bundle"));
                }
            }
        };
        IntentFilter filter = new IntentFilter(ACTION_BASE_BROADCAST);
        ContextCompat.registerReceiver(mActivity, mBroadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterBroadcastReceiver() {
        if (mBroadcastReceiver != null) {
            mActivity.unregisterReceiver(mBroadcastReceiver);
            mBroadcastReceiver = null;
        }
    }

    private class MyLifecycleObserver implements DefaultLifecycleObserver {

        @Override
        public void onCreate(@NonNull LifecycleOwner owner) {
            registerBroadcastReceiver();
        }

        @Override
        public void onDestroy(@NonNull LifecycleOwner owner) {
            unregisterBroadcastReceiver();
        }
    }
}
