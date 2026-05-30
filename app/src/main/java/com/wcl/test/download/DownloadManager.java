package com.wcl.test.download;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;

/**
 * DownloadManager 负责管理所有下载任务
 */
public class DownloadManager {

    private static final int MAX_THREAD = 4;
    private static volatile DownloadManager sInstance;

    private final ExecutorService executor;
    private final OkHttpClient client;

    // 已有任务映射
    private final Map<String, DownloadTask> taskMap = new ConcurrentHashMap<>();
    // 正在下载的 Worker 映射
    private final Map<String, DownloadWorker> workerMap = new ConcurrentHashMap<>();
    // 等待下载队列
    private final List<String> waitingQueue = new CopyOnWriteArrayList<>();

    private DownloadManager() {
        executor = Executors.newFixedThreadPool(MAX_THREAD);
        client = new OkHttpClient();

        // 加载数据库已有任务
        for (DownloadTask t : DownloadDBHelper.ins().loadAllTasks()) {
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

    //==================== 下载行为 ====================

    /**
     * 开始或恢复下载任务
     * 如果任务不存在 → 创建任务并立即启动
     * 如果线程池满 → 状态 WAITING，排队等待
     */
    public void start(String url) {
        if (DownloadUtils.isInvalidUrl(url)) return;

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        if (task == null) {
            task = new DownloadTask(taskId, url, DownloadUtils.getDownloadPath(url));
            taskMap.put(taskId, task);
            DownloadDBHelper.ins().saveTask(task);
        }

        // 已有 Worker，直接返回
        if (workerMap.containsKey(taskId)) return;

        // 判断线程池是否已满
        if (workerMap.size() >= MAX_THREAD) {
            task.status = DownloadTask.Status.WAITING;
            notifyStatus(task);
            if (!waitingQueue.contains(taskId)) waitingQueue.add(taskId);
            return;
        }

        // 创建 Worker 并启动
        createAndStartWorker(taskId, task);
    }

    /**
     * 暂停 url 对应任务
     */
    public void pause(String url) {
        DownloadWorker w = workerMap.get(DownloadUtils.getTaskId(url));
        if (w != null) w.pause();
    }

    /**
     * 删除 url 对应任务
     * 删除任务时保留 listener，同时回调 onDeleted(url)
     */
    public String delete(String url) {
        if (TextUtils.isEmpty(url)) {
            return url;
        }
        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        DownloadWorker worker = workerMap.remove(taskId);
        if (worker != null) {
            worker.pause();
        }

        if (task != null) {
            new File(task.savePath).delete();
            new File(task.savePath + ".temp").delete();
            DownloadDBHelper.ins().deleteTask(taskId);
        }

        taskMap.remove(taskId);
        waitingQueue.remove(taskId);

        // 任务被删除，回调 listener
        for (DownloadListener listener : DownloadUtils.listeners) {
            listener.onDeleted(task != null ? task.url : null);
        }

        return taskId;
    }

    //==================== 监听行为 ====================

    /**
     * 设置下载监听器
     * 订阅行为：
     * 1.如果任务不存在 → 创建任务但不启动
     * 2.注册 listener 到任务或待启动任务
     */
    public void addListener(DownloadListener listener) {
        if (listener == null) return;
        if (!DownloadUtils.listeners.contains(listener)) {
            DownloadUtils.listeners.add(listener);
        }
    }

    /**
     * 移除指定下载监听器
     */
    public void removeListener(DownloadListener listener) {
        if (listener == null) return;
        DownloadUtils.listeners.removeIf(l -> l == listener);
    }

    /**
     * 获取 url 对应任务
     */
    public DownloadTask getTask(String url) {
        return taskMap.get(DownloadUtils.getTaskId(url));
    }

    /**
     * 获取所有任务列表
     */
    public List<DownloadTask> getTasks() {
        return new ArrayList<>(taskMap.values());
    }

    //-----------内部使用-----------

    /**
     * 创建 Worker 并启动
     */
    private void createAndStartWorker(String taskId, DownloadTask task) {
        task.status = DownloadTask.Status.DOWNLOADING;
        DownloadWorker worker = new DownloadWorker(task, client, () -> workerFinished(taskId));
        workerMap.put(taskId, worker);
        executor.execute(worker);
    }

    private void workerFinished(String taskId) {
        workerMap.remove(taskId);

        // 线程池空出来了，启动等待队列中的任务
        if (!waitingQueue.isEmpty()) {
            String nextTaskId = waitingQueue.remove(0);
            DownloadTask nextTask = taskMap.get(nextTaskId);
            if (nextTask != null) {
                createAndStartWorker(nextTaskId, nextTask);
            }
        }
    }

    private void notifyStatus(DownloadTask task) {
        for (DownloadListener listener : DownloadUtils.listeners) {
            listener.onStatusChanged(task);
        }
    }
}
