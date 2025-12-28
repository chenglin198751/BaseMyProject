package com.wcl.test.widget;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;

public class BaseWebViewActivity extends BaseActivity {

    private static final String EXTRA_URL = "url";
    private static final String EXTRA_TITLE = "title";

    private BaseWebViewFragment mWebViewFragment;
    private String mUrl;
    private String mTitle;

    public static void start(Context context, String url, String title) {
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Intent intent = new Intent(context, BaseWebViewActivity.class);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_TITLE, title);

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
        mWebViewFragment = (BaseWebViewFragment) fm.findFragmentById(R.id.fragment_base_id);
        if (mWebViewFragment == null) {
            mWebViewFragment = BaseWebViewFragment.newInstance(mUrl);
            fm.beginTransaction().replace(R.id.fragment_base_id, mWebViewFragment).commit();
        }
    }

    private void parseParams() {
        Intent intent = getIntent();
        mUrl = intent.getStringExtra(EXTRA_URL);
        mTitle = intent.getStringExtra(EXTRA_TITLE);

        if (!TextUtils.isEmpty(mUrl)) {
            try {
                Uri uri = Uri.parse(mUrl);
                String titleFromUrl = uri.getQueryParameter("title");
                if (!TextUtils.isEmpty(titleFromUrl)) {
                    mTitle = titleFromUrl;
                }
            } catch (Exception ignored) {
            }
        }

        if (!TextUtils.isEmpty(mTitle)) {
            getTitleHelper().setTitle(mTitle);
        }
    }
}
