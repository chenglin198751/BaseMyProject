package com.wcl.test.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.wcl.test.R;

public class BaseWebViewActivity extends BaseActivity {

    private static final String KEY_URL = "url";
    private static final String KEY_TITLE = "title";

    private String url;
    private String title;

    public static void start(Context context, String url, String title) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(context, BaseWebViewActivity.class);
        intent.putExtra(KEY_URL, url);
        intent.putExtra(KEY_TITLE, title);

        if (!(context instanceof android.app.Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.my_webview_layout);
        parseParams();

        FragmentManager fm = getSupportFragmentManager();
        BaseWebViewFragment mWebViewFragment = (BaseWebViewFragment) fm.findFragmentById(R.id.fragment_base_id);
        if (mWebViewFragment == null) {
            mWebViewFragment = BaseWebViewFragment.newInstance(url);
            fm.beginTransaction().replace(R.id.fragment_base_id, mWebViewFragment).commit();
        }
    }

    private void parseParams() {
        Intent intent = getIntent();
        url = intent.getStringExtra(KEY_URL);
        title = intent.getStringExtra(KEY_TITLE);

        if (!TextUtils.isEmpty(url)) {
            try {
                Uri uri = Uri.parse(url);
                String titleFromUrl = uri.getQueryParameter("title");
                if (!TextUtils.isEmpty(titleFromUrl)) {
                    title = titleFromUrl;
                }
            } catch (Exception ignored) {
            }
        }

        if (!TextUtils.isEmpty(title)) {
            getTitleHelper().setTitle(title);
        }
    }
}
