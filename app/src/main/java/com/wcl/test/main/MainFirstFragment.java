package com.wcl.test.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.httpwork.HttpUtils;
import com.wcl.test.test.TestPullDownRefreshActivity;
import com.wcl.test.utils.AppBaseUtils;


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
            Intent intent = new Intent(getContext(), TestPullDownRefreshActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        super.onBroadcastReceiver(action, bundle);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }


}
