package com.wcl.test.download;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

public class DownloadManager {
    private static final int MAX_THREAD = 4;

    private static volatile DownloadManager instance;
    private final ExecutorService executor;
    private final OkHttpClient client;
    private final DownloadDBHelper dbHelper;
    private final Map<String, DownloadWorker> workerMap;
    private final Map<String, DownloadTask> taskMap;

    private DownloadManager() {
        this.executor = Executors.newFixedThreadPool(MAX_THREAD);
        this.client = new OkHttpClient();
        this.dbHelper = new DownloadDBHelper();
        this.workerMap = Collections.synchronizedMap(new HashMap<>());
        this.taskMap = Collections.synchronizedMap(new HashMap<>());

        loadTasksFromDB();
    }

    public static DownloadManager ins() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
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
     * @param totalBytes 文件总大小（0 可在下载中获取）
     * @param callback   回调
     */
    public void enqueue(String url, long totalBytes, DownloadCallback callback) {
        if (!DownloadUtils.isValidUrl(url)) {
            if (callback != null)
                callback.onStatusChanged(url, DownloadTask.Status.STATUS_ERROR, "Invalid URL");
            return;
        }

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);
        if (task == null) {
            task = new DownloadTask(url, totalBytes);
            taskMap.put(task.taskId, task);
            dbHelper.saveTask(task);
        }

        // 避免重复下载
        if (workerMap.containsKey(task.taskId)) {
            if (callback != null)
                callback.onStatusChanged(task.taskId, DownloadTask.Status.STATUS_DOWNLOADING, "Already downloading");
            return;
        }

        DownloadWorker worker = getDownloadWorker(callback, task);
        workerMap.put(task.taskId, worker);
        executor.submit(worker);
    }

    @NonNull
    private DownloadWorker getDownloadWorker(DownloadCallback callback, DownloadTask task) {
        return new DownloadWorker(task, new DownloadCallback() {
            @Override
            public void onProgress(String tId, long totalBytes, double progress) {
                if (callback != null)
                    callback.onProgress(tId, totalBytes, progress);
            }

            @Override
            public void onStatusChanged(String tId, DownloadTask.Status status, String errorMsg) {
                task.status = status;
                dbHelper.saveTask(task);
                if (callback != null) callback.onStatusChanged(tId, status, errorMsg);

                if (status == DownloadTask.Status.STATUS_FINISHED ||
                        status == DownloadTask.Status.STATUS_ERROR ||
                        status == DownloadTask.Status.STATUS_CANCELED ||
                        status == DownloadTask.Status.STATUS_PAUSED) {
                    workerMap.remove(tId);
                }
            }

            @Override
            public void onFinished(String tId, String filePath) {
                if (callback != null) callback.onFinished(tId, filePath);
            }
        }, client);
    }

    /**
     * 暂停任务
     */
    public void pause(String url) {
        String taskId = DownloadUtils.getTaskId(url);
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.pause();
        } else {
            DownloadTask task = taskMap.get(taskId);
            if (task != null && task.status == DownloadTask.Status.STATUS_DOWNLOADING) {
                task.status = DownloadTask.Status.STATUS_PAUSED;
                dbHelper.saveTask(task);
            }
        }
    }

    /**
     * 恢复任务
     */
    public void resume(String url, DownloadCallback callback) {
        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);
        if (task != null && (task.status == DownloadTask.Status.STATUS_PAUSED ||
                task.status == DownloadTask.Status.STATUS_ERROR)) {
            enqueue(task.url, task.totalBytes, callback);
        }
    }

    /**
     * 取消任务
     */
    public void cancel(String url) {
        String taskId = DownloadUtils.getTaskId(url);
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.cancel();
        } else {
            DownloadTask task = taskMap.get(taskId);
            if (task != null) {
                task.status = DownloadTask.Status.STATUS_CANCELED;
                dbHelper.saveTask(task);
            }
        }
    }

    /**
     * 获取任务信息
     */
    public DownloadTask getTask(String url) {
        return taskMap.get(DownloadUtils.getTaskId(url));
    }
}
