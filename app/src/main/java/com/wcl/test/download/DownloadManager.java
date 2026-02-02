package com.wcl.test.download;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    /**
     * 开始下载/恢复下载
     */
    public void start(String url, DownloadCallback2 callback) {
        if (!DownloadUtils.isValidUrl(url)) return;

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        if (task == null) {
            task = new DownloadTask(taskId, url, DownloadUtils.getDownloadPath(url));
            taskMap.put(taskId, task);
            dbHelper.saveTask(task);
        }

        // 已完成直接回调所有回调
        File target = new File(task.savePath);
        if (target.exists() && task.totalBytes > 0 && target.length() == task.totalBytes) {
            task.status = DownloadTask.Status.FINISHED;
            task.downloadedBytes = task.totalBytes;
            task.progress = 100.0;

            DownloadWorker existingWorker = workerMap.get(taskId);
            if (existingWorker != null) {
                existingWorker.addCallback(callback);
                existingWorker.notifyStatus();
            } else if (callback != null) {
                DownloadUtils.runOnUiThread(() -> callback.onStatusChanged(taskId));
            }
            return;
        }

        // 已有 Worker，直接添加回调
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.addCallback(callback);
            return;
        }

        // 新建 Worker
        worker = new DownloadWorker(task, client, () -> workerFinished(taskId));
        if (callback != null) worker.addCallback(callback);
        workerMap.put(taskId, worker);
        executor.execute(worker);
    }

    /**
     * 暂停任务
     */
    public void pause(String url) {
        DownloadWorker w = workerMap.get(DownloadUtils.getTaskId(url));
        if (w != null) w.pause();
    }

    /**
     * 删除任务：停止下载 + 回调 DELETED + 删除文件 + 数据库
     */
    public void delete(String url) {
        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        DownloadWorker worker = workerMap.remove(taskId);
        if (worker != null) {
            if (task != null) {
                task.status = DownloadTask.Status.DELETED;
            }
            worker.notifyStatus();
            worker.delete();
            worker.clearCallbacks();
        }

        if (task != null) {
            File target = new File(task.savePath);
            if (target.exists()) target.delete();
            File temp = new File(task.savePath + ".temp");
            if (temp.exists()) temp.delete();

            dbHelper.deleteTask(taskId);
        }

        taskMap.remove(taskId);
    }

    private void workerFinished(String taskId) {
        DownloadWorker worker = workerMap.remove(taskId);
        if (worker != null) worker.clearCallbacks();

        DownloadTask task = taskMap.get(taskId);
        if (task != null) dbHelper.saveTask(task);
    }

    public DownloadTask getTask(String url) {
        return taskMap.get(DownloadUtils.getTaskId(url));
    }

    public List<DownloadTask> getTasks() {
        return (List<DownloadTask>) taskMap.values();
    }
}
