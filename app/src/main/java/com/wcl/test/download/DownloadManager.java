package com.wcl.test.download;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class DownloadManager {

    private static DownloadManager instance;

    private final ExecutorService executor;
    private final OkHttpClient client;
    private final DownloadDBHelper dbHelper;
    private final Map<String, DownloadWorker> workerMap; // 正在下载的 Worker
    private final Map<String, DownloadTask> taskMap;     // 所有任务缓存

    private DownloadManager(Context context, int maxThread) {
        this.executor = Executors.newFixedThreadPool(maxThread);
        this.client = new OkHttpClient();
        this.dbHelper = new DownloadDBHelper(context.getApplicationContext());
        this.workerMap = Collections.synchronizedMap(new HashMap<>());
        this.taskMap = Collections.synchronizedMap(new HashMap<>());

        loadTasksFromDB();
    }

    public static DownloadManager getInstance(Context context, int maxThread) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager(context, maxThread);
                }
            }
        }
        return instance;
    }

    // 加载 DB 中已有任务
    private void loadTasksFromDB() {
        for (DownloadTask task : dbHelper.loadAllTasks()) {
            taskMap.put(task.taskId, task);
        }
    }

    /**
     * 添加下载任务
     *
     * @param url        下载地址
     * @param savePath   文件保存路径
     * @param totalBytes 文件总大小（0 可在下载中获取）
     * @param callback   回调
     */
    public void enqueue(String url, String savePath, long totalBytes, DownloadCallback callback) {
        if (!DownloadUtils.isValidUrl(url)) {
            if (callback != null)
                callback.onStatusChanged(url, DownloadTask.STATUS_ERROR, "Invalid URL");
            return;
        }

        String taskId = DownloadUtils.md5(url + savePath);
        DownloadTask task = taskMap.get(taskId);
        if (task == null) {
            task = new DownloadTask(url, savePath, totalBytes);
            taskMap.put(task.taskId, task);
            dbHelper.saveTask(task);
        }

        // 避免重复下载
        if (workerMap.containsKey(task.taskId)) {
            if (callback != null)
                callback.onStatusChanged(task.taskId, DownloadTask.STATUS_DOWNLOADING, "Already downloading");
            return;
        }

        // 创建 Worker 并提交
        DownloadWorker worker = getDownloadWorker(callback, task);

        workerMap.put(task.taskId, worker);
        executor.submit(worker);
    }

    @NonNull
    private DownloadWorker getDownloadWorker(DownloadCallback callback, DownloadTask task) {
        DownloadWorker worker = new DownloadWorker(task, new DownloadCallback() {
            @Override
            public void onProgress(String tId, long downloadedBytes, long totalBytes, double progress) {
                if (callback != null)
                    callback.onProgress(tId, downloadedBytes, totalBytes, progress);
            }

            @Override
            public void onStatusChanged(String tId, int status, String errorMsg) {
                task.status = status;
                dbHelper.saveTask(task); // 状态变更持久化
                if (callback != null) callback.onStatusChanged(tId, status, errorMsg);

                if (status == DownloadTask.STATUS_FINISHED ||
                        status == DownloadTask.STATUS_ERROR ||
                        status == DownloadTask.STATUS_CANCELED ||
                        status == DownloadTask.STATUS_PAUSED) {
                    workerMap.remove(tId);
                }
            }

            @Override
            public void onFinished(String tId, String filePath) {
                if (callback != null) callback.onFinished(tId, filePath);
            }
        }, client);
        return worker;
    }

    /**
     * 暂停任务
     */
    public void pause(String taskId) {
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.pause();
        } else {
            DownloadTask task = taskMap.get(taskId);
            if (task != null && task.status == DownloadTask.STATUS_DOWNLOADING) {
                task.status = DownloadTask.STATUS_PAUSED;
                dbHelper.saveTask(task);
            }
        }
    }

    /**
     * 恢复任务
     */
    public void resume(String taskId, DownloadCallback callback) {
        DownloadTask task = taskMap.get(taskId);
        if (task != null && (task.status == DownloadTask.STATUS_PAUSED || task.status == DownloadTask.STATUS_ERROR)) {
            enqueue(task.url, task.savePath, task.totalBytes, callback);
        }
    }

    /**
     * 取消任务
     */
    public void cancel(String taskId) {
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.cancel();
        } else {
            DownloadTask task = taskMap.get(taskId);
            if (task != null) {
                task.status = DownloadTask.STATUS_CANCELED;
                dbHelper.saveTask(task);
            }
        }
    }

    /**
     * 获取任务信息
     */
    public DownloadTask getTask(String taskId) {
        return taskMap.get(taskId);
    }
}
