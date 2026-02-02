package com.wcl.test.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.download.DownloadCallback2;
import com.wcl.test.download.DownloadManager;
import com.wcl.test.download.DownloadTask;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {
    private MainFirstFragLayoutBinding mViewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mViewBinding = MainFirstFragLayoutBinding.bind(((ViewGroup) view).getChildAt(0));

        mViewBinding.viewLeft.setOnClickListener(v -> {
            String url ="https://www.wanandroid.com/banner/json";
            HttpUtils.post(getContext(), url, null, new HttpUtils.HttpCallback() {
                @Override
                public void onResult(boolean success, String result) {
                    Log.v("HttpUtils","success:"+success + ",result:"+result);
                }
            });

            String url2 = "http://qd.shouji.qihucdn.com/media/949541c42745397670cd5935dd89835b/674fba4e6f5e6.zip";
            HttpUtils.download(url2, new HttpUtils.DownloadCallback() {
                @Override
                public void onProgress(long total, long current, float percent) {
                    Log.v("tag_99","percent="+percent);
                }

                @Override
                public void onFinished(boolean success, String filePath, String error) {
                    Log.v("tag_99","success:"+success + ",filePath:"+filePath + ",error:"+error);
                }
            });
        });

        String url = "http://qd.shouji.qihucdn.com/media/d22eee36c269dcae8dbfc6a469d02ffc/6602326c507c2.png";
//        SmartImageLoader.load(mViewBinding.image2,url,-1,-1,0);
        mViewBinding.image2.loadImage(url);


    }

    @Override
    public void onEvent(String eventKey, Object data) {
        super.onEvent(eventKey, data);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }


}
