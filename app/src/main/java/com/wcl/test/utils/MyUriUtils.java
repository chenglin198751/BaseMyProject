package com.wcl.test.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.wcl.test.widget.BaseWebViewActivity;

/**
 * Created by chenglin on 2017-8-23.
 */

public class MyUriUtils {
    //标准scheme格式范例
    public static final String test_url_scheme = "mls://shop/goodsDetail?goodsId=10011002";

    /**
     * 用Uri打开一个Activity，或者用url 打开一个webview
     */
    public static void start(Context context, String uri) {
        if (context == null || TextUtils.isEmpty(uri)) {
            return;
        }
        if (uri.toLowerCase().startsWith("mls://")) {
            Uri data = Uri.parse(uri);
            LogUtils.v("tag_99", "host=" + data.getHost() + ",path=" + data.getPath() + ",query=" + data.getQuery());
            String goodsId = data.getQueryParameter("goodsId");
            LogUtils.v("tag_99", "goodsId = " + goodsId);
        } else if (uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://")) {
            BaseWebViewActivity.start(context, uri, null);
        }
    }
}
