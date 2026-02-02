package com.wcl.test.download;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class DownloadManager {

    private static final int MAX_THREAD = 4;
    private static volatile DownloadManager sInstance;

    private final ExecutorService executor;
    private final OkHttpClient client;
    private final DownloadDBHelper dbHelper;

    private final Map<String, DownloadTask> taskMap;
    private final Map<String, DownloadWorker> workerMap;

    private DownloadManager() {
        executor = Executors.newFixedThreadPool(MAX_THREAD);
        client = new OkHttpClient();
        dbHelper = new DownloadDBHelper();

        taskMap = Collections.synchronizedMap(new HashMap<>());
        workerMap = Collections.synchronizedMap(new HashMap<>());

        for (DownloadTask t : dbHelper.loadAllTasks()) {
            taskMap.put(t.taskId, t);
        }
    }

    public static DownloadManager ins() {
        if (sInstance == null) {
            synchronized (DownloadManager.class) {
                if (sInstance == null) {
                    sInstance = new DownloadManager();
                }
            }
        }
        return sInstance;
    }

    public void enqueue(String url, long totalBytes, DownloadCallback2 callback) {

        if (!DownloadUtils.isValidUrl(url)) return;

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        if (task == null) {
            task = new DownloadTask(
                    taskId,
                    url,
                    DownloadUtils.getDownloadPath(url),
                    totalBytes
            );
            taskMap.put(taskId, task);
            dbHelper.saveTask(task);
        }

        // ====== 已完成，直接回调 ======
        File target = new File(task.savePath);
        if (target.exists() && task.totalBytes > 0
                && target.length() == task.totalBytes) {

            task.status = DownloadTask.Status.FINISHED;
            DownloadUtils.runOnUiThread(() ->
                    callback.onStatusChanged(taskId));
            return;
        }

        // 正在下载，直接返回
        if (workerMap.containsKey(taskId)) {
            return;
        }

        DownloadWorker worker = new DownloadWorker(
                task,
                wrapCallback(callback),
                client
        );

        workerMap.put(taskId, worker);
        executor.execute(worker);
    }

    private DownloadCallback2 wrapCallback(DownloadCallback2 cb) {
        return new DownloadCallback2() {

            @Override
            public void onProgress(String taskId) {
                if (cb != null) cb.onProgress(taskId);
            }

            @Override
            public void onStatusChanged(String taskId) {
                DownloadTask task = taskMap.get(taskId);
                if (task != null) {
                    dbHelper.saveTask(task);
                    if (task.status != DownloadTask.Status.DOWNLOADING) {
                        workerMap.remove(taskId);
                    }
                }
                if (cb != null) cb.onStatusChanged(taskId);
            }
        };
    }

    public void pause(String url) {
        DownloadWorker w = workerMap.get(DownloadUtils.getTaskId(url));
        if (w != null) w.pause();
    }

    public void resume(String url, DownloadCallback2 cb) {
        enqueue(url, 0, cb);
    }

    public void cancel(String url) {
        DownloadWorker w = workerMap.get(DownloadUtils.getTaskId(url));
        if (w != null) w.cancel();
    }

    public String getTaskId(String url) {
        return DownloadUtils.getTaskId(url);
    }

    public DownloadTask getTask(String url) {
        return taskMap.get(getTaskId(url));
    }
}
