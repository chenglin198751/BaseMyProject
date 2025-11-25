package com.wcl.test.widget;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.wcl.test.httpwork.HttpUtils;
import com.wcl.test.utils.BitmapUtils;

import java.io.File;

public class LongImageView extends WebView {

    private boolean isDestroyed = false;

    public LongImageView(Context context) {
        super(context);
        init();
    }

    public LongImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        WebSettings ws = getSettings();
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setSupportZoom(true);
        ws.setBuiltInZoomControls(true);
        ws.setDisplayZoomControls(false);

        // 更安全的 WebView 配置
        ws.setJavaScriptEnabled(false);
        ws.setAllowFileAccess(false);
        ws.setDomStorageEnabled(false);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
    }

    /**
     * 判断是否已不可用（Activity 销毁 或 WebView 销毁）
     */
    private boolean isUnavailable() {
        if (isDestroyed) return true;

        Context ctx = getContext();
        if (ctx instanceof Activity a) {
            return a.isFinishing() || a.isDestroyed();
        }
        return false;
    }

    /**
     * 加载本地长图
     */
    public void load(final File file, final int showWidth) {
        if (file == null || !file.exists() || isUnavailable()) return;

        int pictureWidth = BitmapUtils.getBitmapSize(file.getAbsolutePath())[0];
        int targetWidth = Math.max(showWidth, 1);
        float scale = targetWidth * 1f / Math.max(1, pictureWidth);

        // 使用 HTML 自动适配宽度，避免 setInitialScale()
        String html =
                "<html><body style='margin:0;padding:0;'>"
                        + "<img style='width:100%;' src='file://" + file.getAbsolutePath() + "'/>"
                        + "</body></html>";

        loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }

    /**
     * 加载网络长图
     */
    public void load(final String url, final int showWidth) {
        load(url, showWidth, null);
    }

    public void load(final String url, final int showWidth, final HttpUtils.HttpDownloadCallback callback) {
        if (TextUtils.isEmpty(url) || !URLUtil.isNetworkUrl(url)) {
            if (callback != null) callback.onFinished(false, null, "Invalid URL");
            return;
        }

        HttpUtils.downloadFile(url, new HttpUtils.HttpDownloadCallback() {

            @Override
            public void onFinished(boolean ok, String filePath, String err) {
                if (isUnavailable()) return;

                if (ok && filePath != null) {
                    load(new File(filePath), showWidth);
                }

                if (callback != null) {
                    callback.onFinished(ok, filePath, err);
                }
            }

            @Override
            public void onProgress(long total, long curr, float percent) {
                if (!isUnavailable() && callback != null) {
                    callback.onProgress(total, curr, percent);
                }
            }
        });
    }

    /**
     * 更安全的 destroy()
     */
    public void safeDestroy() {
        if (isDestroyed) return;
        isDestroyed = true;

        try {
            stopLoading();
            loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            clearHistory();
            removeAllViews();
            super.destroy();
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        safeDestroy();
        super.onDetachedFromWindow();
    }
}
