package com.wcl.test.httpwork;

import android.os.Handler;
import android.os.Looper;

import com.wcl.test.utils.LogUtils;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


/**
 * Created by weichenglin  on 2018/7/9
 */
public class WebSocketExecutor {
    private final static int NORMAL_CLOSURE_STATUS = 1000;
    private final static int OKHTTP_TIMEOUT = 30;
    private final static int DEFAULT_HEARTBEAT_TIMES = 30;
    private final static int DEFAULT_RECONNECT_COUNT = 10;
    private final static int DEFAULT_RECONNECT_SECONDS = 5;
    private final static String WS_URL = "";

    private int mHeartbeatIntervalSeconds = DEFAULT_HEARTBEAT_TIMES;  //心跳时间间隔
    private int mReconnectCount = DEFAULT_RECONNECT_COUNT;  //连接失败后的重连次数
    private int mReconnectIntervalSeconds = DEFAULT_RECONNECT_SECONDS;  //连接失败后每次重连的时间间隔
    private ByteString mHeartbeatBytes = ByteString.encodeUtf8("");

    private static volatile OkHttpClient mOkHttpClient;
    private int mReconnectIndex = 0;
    private SocketListener mSocketListener;
    private WebSocket mSocket;
    private Runnable mReConnectRunnable;
    private Runnable mSendRunnable;
    private Runnable mTimerRunnable;
    private Timer mTimer;
    private boolean isDestroy = false;
    private boolean isConnect = false;
    private int mWhenSendReconnectIndex = 0;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public WebSocketExecutor() {
        initWebSocket();
    }

    private void initWebSocket() {
        if (mOkHttpClient == null) {
            synchronized (WebSocketExecutor.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient.Builder()
                            .readTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .writeTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .connectTimeout(OKHTTP_TIMEOUT, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }

    /**
     * 启动长连接。如果连接失败，会在失败的回调onFailure 里启动重连。
     */
    public void connect(SocketListener listener) {
        mSocketListener = listener;
        Request request = new Request.Builder().url(WS_URL).build();
        mOkHttpClient.newWebSocket(request, new EchoWebSocketListener());
    }

    /**
     * 断开连接时执行销毁动作，避免内存泄漏
     */
    public void disconnect() {
        isDestroy = true;
        if (mReConnectRunnable != null) {
            mHandler.removeCallbacks(mReConnectRunnable);
        }
        if (mSendRunnable != null) {
            mHandler.removeCallbacks(mSendRunnable);
        }
        if (mTimerRunnable != null) {
            mHandler.removeCallbacks(mTimerRunnable);
        }
        if (mSocket != null) {
            mSocket.close(NORMAL_CLOSURE_STATUS, "close");
            mSocket = null;
        }
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        LogUtils.d("socket", "断开连接");
    }

    /**
     * 重连机制，当webSocket连接失败时，启动重连机制。
     */
    private void retryReconnect() {
        if (isDestroy) {
            return;
        }

        if (mReConnectRunnable == null) {
            mReConnectRunnable = new Runnable() {
                @Override
                public void run() {
                    if (!isDestroy) {
                        connect(mSocketListener);
                        mReconnectIndex++;
                    }
                }
            };
        }

        if (mReconnectIndex < mReconnectCount) {
            mHandler.postDelayed(mReConnectRunnable, mReconnectIntervalSeconds * 1000);
        } else {
            mReconnectIndex = 0;
        }
    }

    /**
     * 是否正在重连
     */
    public boolean isRetryReconnecting() {
        if (mReconnectIndex > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 发送数据
     */
    public void send(final ByteString byteString) {
        if (mSendRunnable == null) {
            mSendRunnable = new Runnable() {
                @Override
                public void run() {
                    //每次发送数据时，都需要检查连接是否正常，如果未连接就需要启动连接
                    if (mSocket != null && mSocketListener != null) {
                        if (!isConnect() && !isRetryReconnecting()) {
                            if (mWhenSendReconnectIndex < 10) {
                                mReconnectIndex = 0;
                                connect(mSocketListener);

                                //此变量为普通成员变量，生命周期与外界生成 WebSocketExecutor 单例的生命周期一致。
                                //目的是避免服务器出错时，发送消息时会无限次重连。
                                mWhenSendReconnectIndex++;
                            }
                            return;
                        }
                    }

                    if (mSocket != null && byteString != null) {
                        mSocket.send(byteString);
                    }
                }
            };
        }

        //WebSocket发送消息交给Handler UI 主线程，避免多线程操作的问题
        //其实WebSocket内部的send方法已经实现synchronized线程安全
        if (isUiThread()) {
            mSendRunnable.run();
        } else {
            mHandler.post(mSendRunnable);
        }

    }

    /**
     * webSocket是否和服务器正常连接
     */
    public boolean isConnect() {
        return isConnect;
    }

    /**
     * 配置webSocket的连接参数，具体看代码
     */
    public void setConfigBuilder(ConfigBuilder builder) {
        if (builder != null) {
            if (builder.getHeartbeatIntervalSeconds() > 0) {
                mHeartbeatIntervalSeconds = builder.getHeartbeatIntervalSeconds();
            }
            if (builder.getReconnectCount() > 0) {
                mReconnectCount = builder.getReconnectCount();
            }
            if (builder.getReconnectIntervalSeconds() > 0) {
                mReconnectIntervalSeconds = builder.getReconnectIntervalSeconds();
            }
            mHeartbeatBytes = builder.getHeartbeatBytes();
        }
    }

    /**
     * 设置心跳数据
     */
    public synchronized void setHeartbeatBytes(ByteString heartbeatBytes) {
        if (heartbeatBytes != null) {
            if (!heartbeatBytes.equals(mHeartbeatBytes)) {
                mHeartbeatBytes = heartbeatBytes;
            }
        }
    }

    private final class EchoWebSocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            LogUtils.d("socket", "webSocket onOpen");
            mSocket = webSocket;
            isConnect = true;
            mReconnectIndex = 0;

            if (mSocketListener != null) {
                mSocketListener.onOpen(webSocket, response);
            }
            startHeartbeat();
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            super.onMessage(webSocket, bytes);
            LogUtils.d("socket", "webSocket ByteString onMessage");
            mSocket = webSocket;
            isConnect = true;

            if (mSocketListener != null) {
                mSocketListener.onMessage(webSocket, bytes);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            mSocket = webSocket;
            isConnect = true;
            LogUtils.d("socket", "webSocket String onMessage = " + text);

            if (mSocketListener != null) {
                mSocketListener.onMessage(webSocket, text);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            mSocket = webSocket;
            isConnect = false;

            if (mSocketListener != null) {
                mSocketListener.onClosed(webSocket, code, reason);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            super.onClosing(webSocket, code, reason);
            mSocket = webSocket;
            isConnect = false;
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            LogUtils.d("socket", "webSocket connect onFailure");
            mSocket = webSocket;
            isConnect = false;

            if (mSocketListener != null) {
                mSocketListener.onFailure(webSocket, t, response);
            }
            retryReconnect();
        }

        // 心跳包,定时不停的发送
        private void startHeartbeat() {
            if (mTimer == null) {
                mTimer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (mTimerRunnable == null) {
                            mTimerRunnable = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (!isDestroy) {
                                            if (isConnect() && mSocket != null && mHeartbeatBytes != null) {
                                                mSocket.send(mHeartbeatBytes);
                                            }
                                        } else {
                                            cancel();
                                            if (mTimer != null) {
                                                mTimer.cancel();
                                                mTimer = null;
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                        }
                        mHandler.post(mTimerRunnable);
                    }
                };
                mTimer.schedule(timerTask, 0, mHeartbeatIntervalSeconds * 1000);
            }
        }
    }

    public static final class ConfigBuilder {
        private ByteString mHeartbeatBytes = ByteString.encodeUtf8("");
        private int mHeartbeatIntervalSeconds = -1;
        private int mReconnectCount = -1;
        private int mReconnectIntervalSeconds = -1;

        /**
         * 设置心跳数据
         *
         * @param data ByteString类型
         */
        public ConfigBuilder setHeartbeatBytes(ByteString data) {
            mHeartbeatBytes = data;
            return this;
        }

        public ByteString getHeartbeatBytes() {
            return mHeartbeatBytes;
        }

        /**
         * 设置心跳的时间间隔
         *
         * @param intervalSeconds 单位秒
         */
        public ConfigBuilder setHeartbeatIntervalSeconds(int intervalSeconds) {
            mHeartbeatIntervalSeconds = intervalSeconds;
            return this;
        }

        public int getHeartbeatIntervalSeconds() {
            return mHeartbeatIntervalSeconds;
        }

        /**
         * 设置webSocket连接发生错误时的重试次数
         *
         * @param count Int类型
         */
        public ConfigBuilder setReconnectCount(int count) {
            mReconnectCount = count;
            return this;
        }

        public int getReconnectCount() {
            return mReconnectCount;
        }

        /**
         * 设置webSocket连接发生错误时重试的时间间隔
         *
         * @param intervalSeconds 单位秒
         */
        public ConfigBuilder setReconnectIntervalSeconds(int intervalSeconds) {
            mReconnectIntervalSeconds = intervalSeconds;
            return this;
        }

        public int getReconnectIntervalSeconds() {
            return mReconnectIntervalSeconds;
        }
    }

    /**
     * 判断当前线程是不是UI线程
     */
    private boolean isUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public interface SocketListener {
        void onOpen(WebSocket webSocket, Response response);

        void onMessage(WebSocket webSocket, String text);

        void onMessage(WebSocket webSocket, ByteString bytes);

        void onClosed(WebSocket webSocket, int code, String reason);

        void onFailure(WebSocket webSocket, Throwable t, Response response);
    }
}