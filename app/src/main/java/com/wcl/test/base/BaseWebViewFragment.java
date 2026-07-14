package com.wcl.test.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
    private String url;
    private boolean isViewReady = false;

    // 带 url：view 就绪后自动加载
    public static BaseWebViewFragment newInstance(String url) {
        BaseWebViewFragment f = new BaseWebViewFragment();
        Bundle b = new Bundle();
        b.putString("url", url);
        f.setArguments(b);
        return f;
    }

    // 不带 url：只建 WebView 不加载，等外部调用 loadUrl
    public static BaseWebViewFragment newInstance() {
        return new BaseWebViewFragment();
    }

    @Override
    protected int getContentLayout() {
        return R.layout.base_fragment_webview_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        init(view);
        isViewReady = true;

        // newInstance(url) 带的 url → 自动加载；
        // newInstance() 不带 url，则等外部 loadUrl（若 loadUrl 早于本方法，url 已存下，这里补加载）
        String argUrl = getArguments() != null ? getArguments().getString("url") : null;
        if (argUrl != null) {
            loadUrl(argUrl);
        } else if (url != null) {
            realLoad();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        isViewReady = false;
        destroyWebView();
        super.onDestroyView();
    }

    /**
     * 命令式加载 url，调一次加载一次（语义同 WebView.loadUrl，不做去重）。
     * 若此时 WebView 尚未初始化，先存下 url，待 onViewCreated 就绪后自动补加载。
     */
    public void loadUrl(String url) {
        this.url = url;
        if (isViewReady) {
            realLoad();
        }
    }

    // 获取当前已设置的 url（未设置时为 null，可据此判断是否已加载过）
    public String getUrl() {
        return url;
    }

    private void init(View root) {
        webView = root.findViewById(R.id.web_view);
        setupWebView();
    }

    // 真正执行加载：显示 loading、设置 cookie、加载 url
    private void realLoad() {
        if (webView == null || url == null) {
            return;
        }
        showLoading();
        setCookies(url);
        webView.loadUrl(url);
    }

    private void setupWebView() {
        webView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
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
            public void onPageCommitVisible(WebView view, String url) {
                onWebViewVisible();
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

    private void onWebViewVisible(){
        hideLoading();
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

    private void setCookies(String urlStr) {
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
        cookieManager.flush();
    }
}
