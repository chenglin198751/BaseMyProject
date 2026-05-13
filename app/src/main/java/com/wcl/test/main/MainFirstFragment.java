package com.wcl.test.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;


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

//
        });

        String url = "http://qd.shouji.qihucdn.com/media/d22eee36c269dcae8dbfc6a469d02ffc/6602326c507c2.png";
        mBinding.image2.loadImage(url);

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
