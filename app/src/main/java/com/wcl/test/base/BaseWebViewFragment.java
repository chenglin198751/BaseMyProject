package com.wcl.test.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.wcl.test.R;
import com.wcl.test.utils.AppLogUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseWebViewFragment extends BaseFragment {

    public WebView webView;
    private ProgressBar mPageLoadingProgressBar;
    private String mUrl;

    public static BaseWebViewFragment newInstance(String url) {
        BaseWebViewFragment f = new BaseWebViewFragment();
        Bundle b = new Bundle();
        b.putString("url", url);
        f.setArguments(b);
        return f;
    }

    @Override
    protected int getContentLayout() {
        return R.layout.base_fragment_webview_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        mUrl = getArguments() != null ? getArguments().getString("url") : null;
        init(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        destroyWebView();
        super.onDestroyView();
    }

    private void init(View root) {
        webView = root.findViewById(R.id.web_view);
        mPageLoadingProgressBar = root.findViewById(R.id.progressBar1);
        mPageLoadingProgressBar.setMax(100);

        setupWebView();
        webView.loadUrl(mUrl);
    }

    private void setupWebView() {
        WebSettings s = webView.getSettings();

        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);

        // 安全：禁止 file:// 被 JS 访问
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setUserAgentString(s.getUserAgentString());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            private boolean handleUrl(WebView view, String url) {
//                if (url.startsWith("weixin://wap")) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    getContext().startActivity(intent);
//                    return true;
//                }

                if (url.startsWith("http://") || url.startsWith("https://")) {
                    view.loadUrl(url);
                    return true;
                }

                // 非 http(s) 交给系统
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception ignored) {
                }
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                mPageLoadingProgressBar.setVisibility(View.VISIBLE);
                mPageLoadingProgressBar.setProgress(0);
            }

            @Override
            public void onPageCommitVisible(WebView view, String url) {
                mPageLoadingProgressBar.setVisibility(View.GONE);
                CookieManager.getInstance().flush();
            }

        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                mPageLoadingProgressBar.setProgress(newProgress);
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception ignored) {
            }
        });
    }

    private void destroyWebView() {
        if (webView == null) return;

        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
        webView.clearHistory();
        webView.stopLoading();

        ViewParent parent = webView.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(webView);
        }

        webView.destroy();
        webView = null;
    }

    public static void setCookies(Context context, String urlStr) {
        // 支持的域名白名单
        final List<String> domains = Arrays.asList(
                "app.api.sj.xx.cn"
        );

        String host;
        try {
            host = new URL(urlStr).getHost();
        } catch (Exception e) {
            return;
        }

        if (!domains.contains(host)) {
            return;
        }

        // 要植入的cookies
        Map<String, String> keyValues = new HashMap<>();
        String token = "";
        keyValues.put("token", token);

        // 覆盖整个域名路径（对该 domain 下所有接口和页面生效）
        final String path = "/";

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);

        // 拼 key=value
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : keyValues.entrySet()) {
            if (sb.length() > 0) sb.append("; ");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        String cookieStr = sb + "; domain=" + host + "; path=" + path + "; Secure";
        AppLogUtils.d("cookieStr", cookieStr);

        // 写入 Cookie
        String targetUrl = "https://" + host;
        cookieManager.setCookie(targetUrl, cookieStr);

        // 同步 Cookie
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
            CookieSyncManager.getInstance().sync();
        } else {
            cookieManager.flush();
        }
    }
}
