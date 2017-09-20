package widget;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import base.BaseActivity;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-8-23.
 */

public class MyWebViewActivity extends BaseActivity {
    private MyWebViewFragment mWebViewFragment;
    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.TRANSLUCENT); //防止网页中的视频闪烁的设置
        setContentLayout(R.layout.my_webview_layout);
        parseSchema();
        init();

        getTitleHelper().setReturnListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void parseSchema() {
        Uri uri = getIntent().getData();
        if (uri != null) {
            mUrl = uri.getQueryParameter("url");
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
        mWebViewFragment = MyWebViewFragment.newInstance(mUrl);
        ft.add(R.id.fragment_base_id, mWebViewFragment);
        ft.commitAllowingStateLoss();
    }


}
