package com.wcl.test.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 使用ChatGPT优化的版本，2025-10-15
 * 源码是androidx.localbroadcastmanager.content.LocalBroadcastManager
 * 从源码独立出来，用ChatGPT优化，可以单独使用的类
 */
public final class LocalBroadcastManager {

    private static final String TAG = "LocalBroadcastManager";

    private final Context mAppContext;
    private final HashMap<BroadcastReceiver, ArrayList<ReceiverRecord>> mReceivers = new HashMap<>();
    private final HashMap<String, ArrayList<ReceiverRecord>> mActions = new HashMap<>();
    private final ConcurrentLinkedQueue<BroadcastRecord> mPendingBroadcasts = new ConcurrentLinkedQueue<>();
    private final Handler mHandler;

    private static final Object mLock = new Object();
    private static LocalBroadcastManager mInstance;

    private static final class ReceiverRecord {
        final IntentFilter filter;
        final BroadcastReceiver receiver;
        boolean dead;

        ReceiverRecord(IntentFilter filter, BroadcastReceiver receiver) {
            this.filter = filter;
            this.receiver = receiver;
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
                        if (actionList.isEmpty()) mActions.remove(action);
                    }
                }
            }
        }
    }

    // -------------------- 发送广播 --------------------
    public boolean sendBroadcast(@NonNull Intent intent) {
        ArrayList<ReceiverRecord> receiversToNotify = new ArrayList<>();
        synchronized (mReceivers) {
            ArrayList<ReceiverRecord> entries = mActions.get(intent.getAction());
            if (entries != null) {
                String type = intent.resolveTypeIfNeeded(mAppContext.getContentResolver());
                Uri data = intent.getData();
                String scheme = intent.getScheme();
                Set<String> categories = intent.getCategories();

                for (ReceiverRecord receiver : entries) {
                    if (receiver.dead) continue;
                    int match = receiver.filter.match(intent.getAction(), type, scheme, data, categories, "LocalBroadcastManager");
                    if (match >= 0) receiversToNotify.add(receiver);
                }
            }
        }

        if (!receiversToNotify.isEmpty()) {
            mPendingBroadcasts.add(new BroadcastRecord(intent, receiversToNotify));
            mHandler.post(this::executePendingBroadcasts);
            return true;
        }
        return false;
    }

    public void sendBroadcastSync(@NonNull Intent intent) {
        if (sendBroadcast(intent)) executePendingBroadcasts();
    }

    private void executePendingBroadcasts() {
        BroadcastRecord br;
        while ((br = mPendingBroadcasts.poll()) != null) {
            for (ReceiverRecord rec : br.receivers) {
                if (!rec.dead) {
                    rec.receiver.onReceive(mAppContext, br.intent);
                }
            }
        }
    }
}
