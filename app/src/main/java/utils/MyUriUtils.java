package utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import widget.BaseWebViewActivity;

/**
 * Created by chenglin on 2017-8-23.
 */

public class MyUriUtils {
    public static final String URI_WEBVIEW = "mls://webview";

    /**
     * 用Uri打开一个Activity，或者用url 打开一个webview
     */
    public static void start(Context context, String uri) {
        if (context == null || TextUtils.isEmpty(uri)) {
            return;
        }
        if (uri.toLowerCase().startsWith("mls://")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            context.startActivity(intent);
        } else if (uri.toLowerCase().startsWith("http://") || uri.toLowerCase().startsWith("https://")) {
            BaseWebViewActivity.start(context, uri, null);
        }
    }
}
