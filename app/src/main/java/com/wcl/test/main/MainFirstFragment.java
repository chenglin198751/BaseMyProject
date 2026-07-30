package com.wcl.test.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.download.DownloadManager;
import com.wcl.test.http.OkHttpExecutor;
import com.wcl.test.test.TestDownloadActivity;
import com.wcl.test.utils.ApkInstaller;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {
    private MainFirstFragLayoutBinding mBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mBinding.viewLeft.setOnClickListener(v -> {
            Log.d("tag_99","22 DownloadManager.ins().getTasks().size()="+ DownloadManager.ins().getTasks().size());
            Intent intent = new Intent(getContext(), TestDownloadActivity.class);
            startActivity(intent);
        });


        String url = "http://qd.shouji.qihucdn.com/media/d22eee36c269dcae8dbfc6a469d02ffc/6602326c507c2.png";
        mBinding.image2.loadImage(url);

//        showWaitDialog();
        showLoading();
    }

    @Override
    public void onEvent(String eventKey, Object data) {
        super.onEvent(eventKey, data);
    }

    @Override
    protected int getContentLayout() {
        return 0;
    }

    @Override
    protected View getContentView() {
        mBinding = MainFirstFragLayoutBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding = null;
    }


}
