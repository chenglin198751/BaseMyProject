package com.wcl.test.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.qihoo360.common.ad.XmAdStrategy;
import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.test.TestRecyclerViewRefreshActivity;


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
            XmAdStrategy.shouldShowAd("aaaaa11",XmAdStrategy.play_apk);
        });
    }

    @Override
    public void onBroadcastReceiver(String eventKey, Object data) {
        super.onBroadcastReceiver(eventKey, data);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }


}
