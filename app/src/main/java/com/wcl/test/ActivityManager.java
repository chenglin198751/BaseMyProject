package com.wcl.test;

import android.net.Uri;
import android.util.Log;

public class ActivityManager {

    public static void startWebView(String url){
//        String url = "mls://user/profile?id=1001";

        Uri uri = Uri.parse(url);

        Log.d("Router", "scheme=" + uri.getScheme());
        Log.d("Router", "host=" + uri.getHost());
        Log.d("Router", "path=" + uri.getPath());
        Log.d("Router", "id=" + uri.getQueryParameter("id"));
    }

}
