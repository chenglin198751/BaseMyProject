package com.wcl.test.http;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wcl.test.utils.AppLogUtils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * WebSocket 管理器
 * 优化点：
 * 1. 修复线程安全问题，使用 AtomicBoolean 和 AtomicInteger
 * 2. 使用 ScheduledExecutorService 替代 Timer，更高效且线程安全
 * 3. 优化资源释放逻辑，避免内存泄漏
 * 4. 添加连接状态枚举，状态管理更清晰
 * 5. 优化重连机制，避免重复连接
 * 6. 添加空指针检查和异常处理
 * 7. 支持主动取消重连任务
 * 8. 优化日志输出
 *
 * Created by weichenglin on 2018/7/9
 */
public class WebSocketExecutor {
    private static final String TAG = "WebSocketExecutor";
    private static final int NORMAL_CLOSURE_STATUS = 1000;
    private static final int OKHTTP_TIMEOUT = 30;
    private static final int DEFAULT_HEARTBEAT_INTERVAL = 30;
    private static final int DEFAULT_RECONNECT_COUNT = 10;
    private static final int DEFAULT_RECONNECT_INTERVAL = 5;
    private static final int MAX_SEND_RECONNECT_COUNT = 10;
    private static final String WS_URL = ""; // TODO: 配置实际的 WebSocket URL

    // 连接状态枚举
    private enum ConnectionState {
        DISCONNECTED,   // 未连接
        CONNECTING,     // 连接中
        CONNECTED,      // 已连接
        DISCONNECTING   // 断开中
    }

    // 配置参数
    private int mHeartbeatIntervalSeconds = DEFAULT_HEARTBEAT_INTERVAL;
    private int mReconnectCount = DEFAULT_RECONNECT_COUNT;
    private int mReconnectIntervalSeconds = DEFAULT_RECONNECT_INTERVAL;
    private ByteString mHeartbeatBytes = ByteString.encodeUtf8("");

    // OkHttpClient 单例（整个应用共享）
    private static volatile OkHttpClient sOkHttpClient;

    // 连接状态相关
    private final AtomicInteger mCurrentReconnectIndex = new AtomicInteger(0);
    private final AtomicInteger mSendReconnectIndex = new AtomicInteger(0);
    private final AtomicBoolean mIsDestroyed = new AtomicBoolean(false);
    private volatile ConnectionState mConnectionState = ConnectionState.DISCONNECTED;

    // WebSocket 相关
    private volatile WebSocket mWebSocket;
    private volatile SocketListener mSocketListener;

    // 线程调度相关
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private ScheduledExecutorService mScheduledExecutor;
    private ScheduledFuture<?> mHeartbeatFuture;
    private Runnable mReconnectRunnable;

    public WebSocketExecutor() {
        initWebSocket();
        initScheduledExecutor();
    }

    /**
     * 初始化 OkHttpClient（双重检查锁单例）
     */
    private void initWebSocket() {
        if (sOkHttpClient == null) {
            synchronized (WebSocketExecutor.class) {
                if (sOkHttpClient == null) {
                    sOkHttpClient = new OkHttpClient.Builder()
                            .readTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .connectTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true) // 启用自动重试
                            .build();
                }
            }
        }
    }

    /**
     * 初始化线程调度器
     */
    private void initScheduledExecutor() {
        if (mScheduledExecutor == null || mScheduledExecutor.isShutdown()) {
            mScheduledExecutor = new ScheduledThreadPoolExecutor(1, r -> {
                Thread thread = new Thread(r, "WebSocket-Scheduler");
                thread.setDaemon(true); // 设置为守护线程
                return thread;
            });
        }
    }

    /**
     * 连接 WebSocket
     *
     * @param listener 连接状态监听器
     */
    public synchronized void connect(@NonNull SocketListener listener) {
        if (mIsDestroyed.get()) {
            AppLogUtils.w(TAG, "WebSocket 已销毁，无法连接");
            return;
        }

        // 避免重复连接
        if (mConnectionState == ConnectionState.CONNECTED ||
                mConnectionState == ConnectionState.CONNECTING) {
            AppLogUtils.d(TAG, "WebSocket 正在连接或已连接，状态: " + mConnectionState);
            return;
        }

        mSocketListener = listener;
        mConnectionState = ConnectionState.CONNECTING;

        try {
            Request request = new Request.Builder()
                    .url(WS_URL)
                    .build();
            sOkHttpClient.newWebSocket(request, new EchoWebSocketListener());
            AppLogUtils.d(TAG, "开始连接 WebSocket");
        } catch (Exception e) {
            AppLogUtils.e(TAG, "连接 WebSocket 异常: " + e.getMessage());
            mConnectionState = ConnectionState.DISCONNECTED;
            scheduleReconnect();
        }
    }

    /**
     * 断开连接并释放资源
     */
    public synchronized void disconnect() {
        AppLogUtils.d(TAG, "主动断开 WebSocket 连接");
        mIsDestroyed.set(true);
        mConnectionState = ConnectionState.DISCONNECTING;

        // 取消重连任务
        cancelReconnect();

        // 停止心跳
        stopHeartbeat();

        // 关闭 WebSocket
        closeWebSocket();

        // 关闭调度器
        shutdownScheduler();

        mConnectionState = ConnectionState.DISCONNECTED;
        mSocketListener = null;
    }

    /**
     * 关闭 WebSocket 连接
     */
    private void closeWebSocket() {
        WebSocket socket = mWebSocket;
        if (socket != null) {
            try {
                socket.close(NORMAL_CLOSURE_STATUS, "Client closed connection");
            } catch (Exception e) {
                AppLogUtils.e(TAG, "关闭 WebSocket 异常: " + e.getMessage());
            } finally {
                mWebSocket = null;
            }
        }
    }

    /**
     * 关闭调度器
     */
    private void shutdownScheduler() {
        if (mScheduledExecutor != null && !mScheduledExecutor.isShutdown()) {
            try {
                mScheduledExecutor.shutdown();
                if (!mScheduledExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    mScheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                mScheduledExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * 重连机制
     */
    private void scheduleReconnect() {
        if (mIsDestroyed.get()) {
            return;
        }

        int currentIndex = mCurrentReconnectIndex.get();
        if (currentIndex >= mReconnectCount) {
            AppLogUtils.w(TAG, "重连次数已达上限: " + mReconnectCount);
            mCurrentReconnectIndex.set(0);
            notifyReconnectFailed();
            return;
        }

        // 取消之前的重连任务
        cancelReconnect();

        mReconnectRunnable = () -> {
            if (!mIsDestroyed.get() && mConnectionState != ConnectionState.CONNECTED) {
                int index = mCurrentReconnectIndex.incrementAndGet();
                AppLogUtils.d(TAG, "尝试第 " + index + " 次重连");

                mMainHandler.post(() -> {
                    if (!mIsDestroyed.get() && mSocketListener != null) {
                        connect(mSocketListener);
                    }
                });
            }
        };

        long delayMillis = mReconnectIntervalSeconds * 1000L;
        mMainHandler.postDelayed(mReconnectRunnable, delayMillis);
        AppLogUtils.d(TAG, "将在 " + mReconnectIntervalSeconds + " 秒后进行第 "
                + (currentIndex + 1) + " 次重连");
    }

    /**
     * 取消重连任务
     */
    private void cancelReconnect() {
        if (mReconnectRunnable != null) {
            mMainHandler.removeCallbacks(mReconnectRunnable);
            mReconnectRunnable = null;
        }
    }

    /**
     * 通知重连失败
     */
    private void notifyReconnectFailed() {
        SocketListener listener = mSocketListener;
        if (listener != null) {
            mMainHandler.post(() -> {
                // 可以添加重连失败的回调
                AppLogUtils.e(TAG, "重连失败，已达最大重连次数");
            });
        }
    }

    /**
     * 检查是否正在重连
     */
    public boolean isReconnecting() {
        return mCurrentReconnectIndex.get() > 0;
    }

    /**
     * 发送数据
     *
     * @param byteString 要发送的数据
     */
    public void send(@Nullable ByteString byteString) {
        if (byteString == null) {
            AppLogUtils.w(TAG, "发送数据为空");
            return;
        }

        if (mIsDestroyed.get()) {
            AppLogUtils.w(TAG, "WebSocket 已销毁，无法发送数据");
            return;
        }

        // 检查连接状态
        if (!isConnected() && !isReconnecting()) {
            int sendReconnectCount = mSendReconnectIndex.get();
            if (sendReconnectCount < MAX_SEND_RECONNECT_COUNT) {
                AppLogUtils.d(TAG, "连接断开，尝试重连后发送");
                mCurrentReconnectIndex.set(0);
                mSendReconnectIndex.incrementAndGet();

                SocketListener listener = mSocketListener;
                if (listener != null) {
                    connect(listener);
                }
            } else {
                AppLogUtils.w(TAG, "发送重连次数已达上限，放弃发送");
            }
            return;
        }

        WebSocket socket = mWebSocket;
        if (socket != null) {
            try {
                boolean success = socket.send(byteString);
                if (!success) {
                    AppLogUtils.w(TAG, "发送数据失败，消息队列可能已满");
                }
            } catch (Exception e) {
                AppLogUtils.e(TAG, "发送数据异常: " + e.getMessage());
            }
        } else {
            AppLogUtils.w(TAG, "WebSocket 连接为空，无法发送数据");
        }
    }

    /**
     * 发送文本数据
     */
    public void send(@Nullable String text) {
        if (text != null) {
            send(ByteString.encodeUtf8(text));
        }
    }

    /**
     * 检查是否已连接
     */
    public boolean isConnected() {
        return mConnectionState == ConnectionState.CONNECTED &&
                mWebSocket != null &&
                !mIsDestroyed.get();
    }

    /**
     * 配置 WebSocket 参数
     */
    public void setConfig(@NonNull ConfigBuilder builder) {
        if (builder.getHeartbeatIntervalSeconds() > 0) {
            mHeartbeatIntervalSeconds = builder.getHeartbeatIntervalSeconds();
        }
        if (builder.getReconnectCount() > 0) {
            mReconnectCount = builder.getReconnectCount();
        }
        if (builder.getReconnectIntervalSeconds() > 0) {
            mReconnectIntervalSeconds = builder.getReconnectIntervalSeconds();
        }
        ByteString heartbeat = builder.getHeartbeatBytes();
        if (heartbeat != null) {
            mHeartbeatBytes = heartbeat;
        }
    }

    /**
     * 设置心跳数据
     */
    public synchronized void setHeartbeatBytes(@Nullable ByteString heartbeatBytes) {
        if (heartbeatBytes != null && !heartbeatBytes.equals(mHeartbeatBytes)) {
            mHeartbeatBytes = heartbeatBytes;
            // 如果心跳已启动，重启心跳以使用新数据
            if (mHeartbeatFuture != null && !mHeartbeatFuture.isCancelled()) {
                stopHeartbeat();
                startHeartbeat();
            }
        }
    }

    /**
     * 启动心跳
     */
    private synchronized void startHeartbeat() {
        if (mIsDestroyed.get()) {
            return;
        }

        // 停止之前的心跳
        stopHeartbeat();

        initScheduledExecutor();

        try {
            mHeartbeatFuture = mScheduledExecutor.scheduleWithFixedDelay(
                    this::sendHeartbeat,
                    0,
                    mHeartbeatIntervalSeconds,
                    TimeUnit.SECONDS
            );
            AppLogUtils.d(TAG, "心跳已启动，间隔: " + mHeartbeatIntervalSeconds + " 秒");
        } catch (Exception e) {
            AppLogUtils.e(TAG, "启动心跳异常: " + e.getMessage());
        }
    }

    /**
     * 停止心跳
     */
    private synchronized void stopHeartbeat() {
        if (mHeartbeatFuture != null && !mHeartbeatFuture.isCancelled()) {
            mHeartbeatFuture.cancel(false);
            mHeartbeatFuture = null;
            AppLogUtils.d(TAG, "心跳已停止");
        }
    }

    /**
     * 发送心跳包
     */
    private void sendHeartbeat() {
        if (mIsDestroyed.get()) {
            stopHeartbeat();
            return;
        }

        WebSocket socket = mWebSocket;
        ByteString heartbeat = mHeartbeatBytes;

        if (isConnected() && socket != null && heartbeat != null) {
            try {
                boolean success = socket.send(heartbeat);
                if (success) {
                    AppLogUtils.v(TAG, "心跳发送成功");
                } else {
                    AppLogUtils.w(TAG, "心跳发送失败");
                }
            } catch (Exception e) {
                AppLogUtils.e(TAG, "发送心跳异常: " + e.getMessage());
            }
        }
    }

    /**
     * WebSocket 监听器
     */
    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(@NonNull WebSocket webSocket, @NonNull Response response) {
            AppLogUtils.d(TAG, "WebSocket 连接成功");
            mWebSocket = webSocket;
            mConnectionState = ConnectionState.CONNECTED;
            mCurrentReconnectIndex.set(0);
            mSendReconnectIndex.set(0);

            startHeartbeat();

            SocketListener listener = mSocketListener;
            if (listener != null) {
                mMainHandler.post(() -> listener.onOpen(webSocket, response));
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull ByteString bytes) {
            AppLogUtils.v(TAG, "收到二进制消息，长度: " + bytes.size());

            SocketListener listener = mSocketListener;
            if (listener != null) {
                mMainHandler.post(() -> listener.onMessage(webSocket, bytes));
            }
        }

        @Override
        public void onMessage(@NonNull WebSocket webSocket, @NonNull String text) {
            AppLogUtils.v(TAG, "收到文本消息: " + text);

            SocketListener listener = mSocketListener;
            if (listener != null) {
                mMainHandler.post(() -> listener.onMessage(webSocket, text));
            }
        }

        @Override
        public void onClosing(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            AppLogUtils.d(TAG, "WebSocket 正在关闭，code: " + code + ", reason: " + reason);
            mConnectionState = ConnectionState.DISCONNECTING;
        }

        @Override
        public void onClosed(@NonNull WebSocket webSocket, int code, @NonNull String reason) {
            AppLogUtils.d(TAG, "WebSocket 已关闭，code: " + code + ", reason: " + reason);
            mConnectionState = ConnectionState.DISCONNECTED;
            stopHeartbeat();

            SocketListener listener = mSocketListener;
            if (listener != null) {
                mMainHandler.post(() -> listener.onClosed(webSocket, code, reason));
            }

            // 非主动断开时尝试重连
            if (!mIsDestroyed.get() && code != NORMAL_CLOSURE_STATUS) {
                scheduleReconnect();
            }
        }

        @Override
        public void onFailure(@NonNull WebSocket webSocket, @NonNull Throwable t,
                              @Nullable Response response) {
            AppLogUtils.e(TAG, "WebSocket 连接失败: " + t.getMessage());
            mConnectionState = ConnectionState.DISCONNECTED;
            stopHeartbeat();

            SocketListener listener = mSocketListener;
            if (listener != null) {
                mMainHandler.post(() -> listener.onFailure(webSocket, t, response));
            }

            // 失败后尝试重连
            if (!mIsDestroyed.get()) {
                scheduleReconnect();
            }
        }
    }

    /**
     * 配置构建器
     */
    public static final class ConfigBuilder {
        private ByteString heartbeatBytes = ByteString.encodeUtf8("");
        private int heartbeatIntervalSeconds = -1;
        private int reconnectCount = -1;
        private int reconnectIntervalSeconds = -1;

        public ConfigBuilder setHeartbeatBytes(@NonNull ByteString data) {
            this.heartbeatBytes = data;
            return this;
        }

        public ByteString getHeartbeatBytes() {
            return heartbeatBytes;
        }

        public ConfigBuilder setHeartbeatIntervalSeconds(int intervalSeconds) {
            this.heartbeatIntervalSeconds = intervalSeconds;
            return this;
        }

        public int getHeartbeatIntervalSeconds() {
            return heartbeatIntervalSeconds;
        }

        public ConfigBuilder setReconnectCount(int count) {
            this.reconnectCount = count;
            return this;
        }

        public int getReconnectCount() {
            return reconnectCount;
        }

        public ConfigBuilder setReconnectIntervalSeconds(int intervalSeconds) {
            this.reconnectIntervalSeconds = intervalSeconds;
            return this;
        }

        public int getReconnectIntervalSeconds() {
            return reconnectIntervalSeconds;
        }
    }

    /**
     * WebSocket 状态监听器
     */
    public interface SocketListener {
        void onOpen(WebSocket webSocket, Response response);

        void onMessage(WebSocket webSocket, String text);

        void onMessage(WebSocket webSocket, ByteString bytes);

        void onClosed(WebSocket webSocket, int code, String reason);

        void onFailure(WebSocket webSocket, Throwable t, Response response);
    }
}