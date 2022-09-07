package com.wcl.test.widget;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


import com.wcl.test.base.BaseFragment;
import com.wcl.test.R;

/**
 * Created by chenglin on 2017-9-20.
 */

public class BaseWebViewFragment extends BaseFragment {
    private WebView mWebView;
    private ProgressBar mPageLoadingProgressBar = null;
    private String mUrl;

    /**
     * @return 构建一个MyWebViewFragment实例
     */
    public static BaseWebViewFragment newInstance(String url) {
        BaseWebViewFragment webViewFragment = new BaseWebViewFragment();
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        webViewFragment.setArguments(bundle);
        return webViewFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.base_fragment_webview_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        parseSchema();
        init();
    }

    private void parseSchema() {
        mUrl = getArguments().getString("url");
    }

    public boolean onBackPressed() {
        if (mWebView != null && mWebView.canGoBack()) {
            mWebView.goBack();
            return false;
        } else {
            getActivity().finish();
            return true;
        }
    }

    @Override
    public void onDestroy() {
        if (mWebView != null) {
            mWebView.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
            mWebView.clearHistory();
            ((ViewGroup) mWebView.getParent()).removeView(mWebView);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }

    private void init() {
        mWebView = (WebView) getView().findViewById(R.id.web_view);
        initProgressBar();

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                if (url.startsWith("weixin://wap")) {
//                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//                    getContext().startActivity(intent);
//                    return true;
//                }
                return false;
            }

            @Override
            public void onPageStarted(WebView webView, String s, Bitmap bitmap) {
                super.onPageStarted(webView, s, bitmap);
                mPageLoadingProgressBar.setVisibility(View.VISIBLE);
                mPageLoadingProgressBar.setProgress(1);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mPageLoadingProgressBar.setVisibility(View.GONE);
            }

        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public boolean onJsConfirm(WebView arg0, String arg1, String arg2, JsResult arg3) {
                return super.onJsConfirm(arg0, arg1, arg2, arg3);
            }

            @Override
            public void onProgressChanged(WebView webView, int newProgress) {
                super.onProgressChanged(webView, newProgress);
                mPageLoadingProgressBar.setProgress(newProgress);
            }

            @Override
            public void onHideCustomView() {
            }

            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(null, url, message, result);
            }
        });

        mWebView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(final String url, final String userAgent,
                                        final String contentDisposition, final String mimetype,
                                        final long contentLength) {
                CommonDialog myDialog = new CommonDialog(getActivity());
                myDialog.setLeftButton(getString(R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });
                myDialog.setRightButton(getString(R.string.cancel), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                myDialog.setMessage(R.string.dialog_download_file_message);
                myDialog.show();
            }
        });

        WebSettings webSetting = mWebView.getSettings();
        webSetting.setAllowFileAccess(true);
        webSetting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        webSetting.setSupportZoom(true);
        webSetting.setBuiltInZoomControls(true);
        webSetting.setUseWideViewPort(true);
        webSetting.setSupportMultipleWindows(false);
        // webSetting.setLoadWithOverviewMode(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setAppCacheEnabled(true);
        // webSetting.setDatabaseEnabled(true);
        webSetting.setDomStorageEnabled(true);
        webSetting.setJavaScriptEnabled(true);
        webSetting.setGeolocationEnabled(true);
        webSetting.setAppCacheMaxSize(Long.MAX_VALUE);
        webSetting.setAppCachePath(getActivity().getDir("appcache", 0).getPath());
        webSetting.setDatabasePath(getActivity().getDir("databases", 0).getPath());
        webSetting.setGeolocationDatabasePath(getActivity().getDir("geolocation", 0).getPath());
        webSetting.setPluginState(WebSettings.PluginState.ON_DEMAND);
        String APP_NAME_UA = "";//自定义UA
        webSetting.setUserAgentString(webSetting.getUserAgentString() + APP_NAME_UA);
        mWebView.loadUrl(mUrl);

        CookieSyncManager.createInstance(getActivity());
        CookieSyncManager.getInstance().sync();
    }

    private void initProgressBar() {
        mPageLoadingProgressBar = (ProgressBar) getView().findViewById(R.id.progressBar1);
        mPageLoadingProgressBar.setMax(100);
        mPageLoadingProgressBar.setProgressDrawable(this.getResources().getDrawable(R.drawable.color_progressbar));
    }

}
