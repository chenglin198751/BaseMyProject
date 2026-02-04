package com.wcl.test.download;

import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final DownloadDBHelper dbHelper;

    // 已有任务映射
    private final Map<String, DownloadTask> taskMap;
    // 正在下载的 Worker 映射
    private final Map<String, DownloadWorker> workerMap;
    // 已注册但任务可能尚不存在的监听器映射
    private final Map<String, List<ListenerHolder>> listenerMap;

    private DownloadManager() {
        executor = Executors.newFixedThreadPool(MAX_THREAD);
        client = new OkHttpClient();
        dbHelper = new DownloadDBHelper();

        taskMap = Collections.synchronizedMap(new HashMap<>());
        workerMap = Collections.synchronizedMap(new HashMap<>());
        listenerMap = Collections.synchronizedMap(new HashMap<>());

        // 加载数据库已有任务
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

    //==================== 下载行为 ====================

    /**
     * 开始或恢复下载任务
     * 如果任务不存在 → 创建任务并立即启动
     */
    public void start(String url) {
        if (!DownloadUtils.isValidUrl(url)) return;

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        if (task == null) {
            task = new DownloadTask(taskId, url, DownloadUtils.getDownloadPath(url));
            taskMap.put(taskId, task);
            dbHelper.saveTask(task);
        }

        // 已有 Worker，直接返回（监听已通过 setDownloadListener 注册）
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) return;

        // 创建 Worker 并启动
        worker = new DownloadWorker(task, client, () -> workerFinished(taskId));

        // 添加已注册的 listener
        List<ListenerHolder> holders = listenerMap.get(taskId);
        if (holders != null) {
            for (ListenerHolder holder : holders) {
                worker.addCallback(holder.owner, holder.listener);
            }
        }

        workerMap.put(taskId, worker);
        executor.execute(worker);
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
     */
    public String deleteByUrl(String url) {
        String taskId = DownloadUtils.getTaskId(url);
        return deleteById(taskId);
    }

    /**
     * 删除 taskId 对应任务
     */
    public String deleteById(String taskId) {
        DownloadTask task = taskMap.get(taskId);

        DownloadWorker worker = workerMap.remove(taskId);
        if (worker != null) {
            worker.pause();
            worker.clearCallbacks();
        }

        if (task != null) {
            new File(task.savePath).delete();
            new File(task.savePath + ".temp").delete();
            dbHelper.deleteTask(taskId);
        }

        taskMap.remove(taskId);
        listenerMap.remove(taskId);
        return taskId;
    }

    //==================== 监听行为 ====================

    /**
     * 设置下载监听器
     * 订阅行为：
     * 1.如果任务不存在 → 创建任务但不启动
     * 2.注册 listener 到任务或待启动任务
     */
    public void setDownloadListener(String url, LifecycleOwner owner, DownloadListener listener) {
        if (!DownloadUtils.isValidUrl(url) || listener == null || owner == null) return;

        String taskId = DownloadUtils.getTaskId(url);
        DownloadTask task = taskMap.get(taskId);

        // 任务不存在 → 创建但不启动
        if (task == null) {
            task = new DownloadTask(taskId, url, DownloadUtils.getDownloadPath(url));
            taskMap.put(taskId, task);
            dbHelper.saveTask(task);
        }

        // 如果已有 Worker，直接添加
        DownloadWorker worker = workerMap.get(taskId);
        if (worker != null) {
            worker.addCallback(owner, listener);
            return;
        }

        // 暂存 listener
        List<ListenerHolder> holders = listenerMap.get(taskId);
        if (holders == null) {
            holders = Collections.synchronizedList(new ArrayList<>());
            listenerMap.put(taskId, holders);
        }
        holders.add(new ListenerHolder(owner, listener));
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

    private void workerFinished(String taskId) {
        DownloadWorker worker = workerMap.remove(taskId);
        if (worker != null) worker.clearCallbacks();

        DownloadTask task = taskMap.get(taskId);
        if (task != null) dbHelper.saveTask(task);
    }

    /**
     * 包装 LifecycleOwner + DownloadListener
     */
    private record ListenerHolder(LifecycleOwner owner, DownloadListener listener) {
    }
}
