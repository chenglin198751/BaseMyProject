package com.wcl.test.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;

import com.wcl.test.base.BaseActivity;
import com.wcl.test.R;

/**
 * Created by chenglin on 2017-8-23.
 */

public class BaseWebViewActivity extends BaseActivity {
    private BaseWebViewFragment mWebViewFragment;
    private String mUrl, mTitle;

    public static void start(Context context, String url, String title) {
        if (TextUtils.isEmpty(url)) {
            throw new NullPointerException("url must is not null");
        }

        Intent intent = new Intent(context, BaseWebViewActivity.class);
        intent.putExtra("url", url);
        intent.putExtra("title", title);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT); //防止网页中的视频闪烁的设置
        setContentLayout(R.layout.my_webview_layout);
        parseParams();
        init();

        getTitleHelper().setReturnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void parseParams() {
        mUrl = getIntent().getStringExtra("url");
        mTitle = getIntent().getStringExtra("title");

        Uri httpUri = Uri.parse(mUrl);
        if (httpUri != null) {
            String title = httpUri.getQueryParameter("title");
            if (!TextUtils.isEmpty(title)) {
                mTitle = title;
            }
            if (!TextUtils.isEmpty(mTitle)) {
                getTitleHelper().setTitle(mTitle);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mWebViewFragment != null) {
            mWebViewFragment.onBackPressed();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
    }

    @Override
    protected void onDestroy() {
        if (mWebViewFragment != null) {
            mWebViewFragment.onDestroy();
        }
        super.onDestroy();
    }

    private void init() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        mWebViewFragment = BaseWebViewFragment.newInstance(mUrl);
        ft.add(R.id.fragment_base_id, mWebViewFragment);
        ft.commitAllowingStateLoss();
    }


}
