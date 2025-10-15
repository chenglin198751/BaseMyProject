package com.wcl.test.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 使用ChatGPT优化的版本，2025-10-15
 */
public final class LocalBroadcastManager {

    private static final String TAG = "LocalBroadcastManager";
    private static final boolean DEBUG = false;

    private final Context mAppContext;
    private final HashMap<BroadcastReceiver, ArrayList<ReceiverRecord>> mReceivers = new HashMap<>();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap<>();
    private final ArrayList<BroadcastRecord> mPendingBroadcasts = new ArrayList<>();
    private final Handler mHandler;

    private static final Object mLock = new Object();
    private static LocalBroadcastManager mInstance;

    // -------------------- 内部类 --------------------
    private static final class ReceiverRecord {
        final IntentFilter filter;
        final BroadcastReceiver receiver;
        boolean broadcasting;
        boolean dead;

        ReceiverRecord(IntentFilter filter, BroadcastReceiver receiver) {
            this.filter = filter;
            this.receiver = receiver;
        }

        @Override
        public String toString() {
            return "Receiver{" + receiver + " filter=" + filter + (dead ? " DEAD" : "") + "}";
        }
    }

    private static final class BroadcastRecord {
        final Intent intent;
        final ArrayList<ReceiverRecord> receivers;

        BroadcastRecord(Intent intent, ArrayList<ReceiverRecord> receivers) {
            this.intent = intent;
            this.receivers = receivers;
        }
    }

    // -------------------- 单例 --------------------
    @NonNull
    public static LocalBroadcastManager getInstance(@NonNull Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new LocalBroadcastManager(context.getApplicationContext());
            }
            return mInstance;
        }
    }

    private LocalBroadcastManager(Context context) {
        mAppContext = context;
        mHandler = new Handler(context.getMainLooper());
    }

    // -------------------- 注册/注销 --------------------
    public void registerReceiver(@NonNull BroadcastReceiver receiver, @NonNull IntentFilter filter) {
        synchronized (mReceivers) {
            ReceiverRecord entry = new ReceiverRecord(filter, receiver);
            mReceivers.computeIfAbsent(receiver, k -> new ArrayList<>()).add(entry);
            for (int i = 0; i < filter.countActions(); i++) {
                String action = filter.getAction(i);
                mActions.computeIfAbsent(action, k -> new ArrayList<>()).add(entry);
            }
        }
    }

    public void unregisterReceiver(@NonNull BroadcastReceiver receiver) {
        synchronized (mReceivers) {
            ArrayList<ReceiverRecord> filters = mReceivers.remove(receiver);
            if (filters == null) return;

            for (ReceiverRecord record : filters) {
                record.dead = true;
                for (int j = 0; j < record.filter.countActions(); j++) {
                    String action = record.filter.getAction(j);
                    ArrayList<ReceiverRecord> actionList = mActions.get(action);
                    if (actionList != null) {
                        actionList.removeIf(r -> r.receiver == receiver);
                        if (actionList.isEmpty()) {
                            mActions.remove(action);
                        }
                    }
                }
            }
        }
    }

    // -------------------- 发送广播 --------------------
    public boolean sendBroadcast(@NonNull Intent intent) {
        ArrayList<ReceiverRecord> receiversToNotify = null;
        synchronized (mReceivers) {
            String action = intent.getAction();
            ArrayList<ReceiverRecord> entries = mActions.get(action);
            if (entries != null) {
                for (ReceiverRecord receiver : entries) {
                    if (receiver.dead || receiver.broadcasting) continue;
                    int match = receiver.filter.match(intent.getAction(),
                            intent.resolveTypeIfNeeded(mAppContext.getContentResolver()),
                            intent.getScheme(), intent.getData(), intent.getCategories(), "LocalBroadcastManager");
                    if (match >= 0) {
                        if (receiversToNotify == null) receiversToNotify = new ArrayList<>();
                        receiversToNotify.add(receiver);
                        receiver.broadcasting = true;
                    }
                }
            }
            if (receiversToNotify != null) {
                for (ReceiverRecord r : receiversToNotify) r.broadcasting = false;
                mPendingBroadcasts.add(new BroadcastRecord(intent, receiversToNotify));
                mHandler.post(this::executePendingBroadcasts);
                return true;
            }
        }
        return false;
    }

    public void sendBroadcastSync(@NonNull Intent intent) {
        if (sendBroadcast(intent)) {
            executePendingBroadcasts();
        }
    }

    private void executePendingBroadcasts() {
        BroadcastRecord[] brs;
        synchronized (mReceivers) {
            if (mPendingBroadcasts.isEmpty()) return;
            brs = mPendingBroadcasts.toArray(new BroadcastRecord[0]);
            mPendingBroadcasts.clear();
        }
        for (BroadcastRecord br : brs) {
            for (ReceiverRecord rec : br.receivers) {
                if (!rec.dead) {
                    rec.receiver.onReceive(mAppContext, br.intent);
                }
            }
        }
    }
}
