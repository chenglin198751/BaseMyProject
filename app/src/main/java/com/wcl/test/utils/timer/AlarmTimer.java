package com.wcl.test.utils.timer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

public class AlarmTimer implements SimpleTimer {
    private static final String ACTION = "ALARM_TIMER_ACTION_" + System.currentTimeMillis();
    private final Context context;
    private final AlarmManager alarmManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private onTickListener callback;

    private PendingIntent pendingIntent;
    private boolean isRunning = false;

    // 配置参数
    private long delayMs = 0;
    private long intervalMs = 0;

    private long nextTriggerTime;

    public AlarmTimer(Context context) {
        this.context = context.getApplicationContext();
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    // ---------- SimpleTimer 接口实现 ---------- //

    @Override
    public SimpleTimer setDelay(long delayMs) {
        this.delayMs = delayMs;
        return this;
    }

    @Override
    public SimpleTimer setInterval(long intervalMs) {
        this.intervalMs = intervalMs;
        return this;
    }

    @Override
    public SimpleTimer onTick(onTickListener callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void start() {
        stop(); // 先停止已有任务
        isRunning = true;

        nextTriggerTime = System.currentTimeMillis() + delayMs;

        registerReceiver();
        scheduleAlarm(nextTriggerTime);
    }

    @Override
    public void stop() {
        isRunning = false;
        cancelAlarm();
        unregisterReceiver();
    }

    // ---------- Alarm 调度 ---------- //

    private void scheduleAlarm(long triggerAtMs) {
        Intent intent = new Intent(ACTION);
        pendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
            }
        } catch (Throwable e) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent);
        }
    }

    private void cancelAlarm() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent = null;
        }
    }

    // ---------- 内部 BroadcastReceiver ---------- //

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!isRunning) return;

            mainHandler.post(() -> {
                if (callback != null) callback.onTick();
            });

            // 循环执行下一次
            nextTriggerTime += intervalMs;
            scheduleAlarm(nextTriggerTime);
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION);
        context.registerReceiver(receiver, filter);
    }

    private void unregisterReceiver() {
        try {
            context.unregisterReceiver(receiver);
        } catch (Exception ignored) {
        }
    }

    // 可选：判断是否运行
    public boolean isRunning() {
        return isRunning;
    }
}
