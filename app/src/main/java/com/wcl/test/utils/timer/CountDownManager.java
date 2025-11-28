package com.wcl.test.utils.timer;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 倒计时管理器类，适用于列表中item展示倒计时秒杀
 */
public class CountDownManager {
        private static final CountDownManager INSTANCE = new CountDownManager();
        private final List<WeakReference<TickListener>> listeners = new ArrayList<>();
        private final Handler handler = new Handler(Looper.getMainLooper());
        private boolean isRunning = false;

        public interface TickListener {
            void onTick();
        }

        public static CountDownManager getInstance() {
            return INSTANCE;
        }

        public void addListener(TickListener listener) {
            synchronized (listeners) {
                listeners.add(new WeakReference<>(listener));
            }
        }

        public void removeListener(TickListener listener) {
            synchronized (listeners) {
                Iterator<WeakReference<TickListener>> it = listeners.iterator();
                while (it.hasNext()) {
                    TickListener l = it.next().get();
                    if (l == null || l == listener) {
                        it.remove();
                    }
                }
            }
        }

        private final Runnable tickRunnable = new Runnable() {
            @Override
            public void run() {
                notifyAllListeners();
                if (isRunning) {
                    handler.postDelayed(this, 1000);
                }
            }
        };

        private void notifyAllListeners() {
            synchronized (listeners) {
                Iterator<WeakReference<TickListener>> it = listeners.iterator();
                while (it.hasNext()) {
                    TickListener l = it.next().get();
                    if (l != null) {
                        l.onTick();
                    } else {
                        it.remove();
                    }
                }
            }
        }

        public void start() {
            if (!isRunning) {
                isRunning = true;
                handler.post(tickRunnable);
            }
        }

        public void stop() {
            isRunning = false;
            handler.removeCallbacks(tickRunnable);
        }
    }