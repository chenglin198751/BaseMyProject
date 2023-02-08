package com.wcl.test.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.test.TestRecyclerViewRefreshActivity;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {
    private MainFirstFragLayoutBinding mViewBinding;

    final String apk_path = "http://qd.shouji.qihucdn.com/media/eb1b2f401965fce6f75198d3a5af1299/6305873c18c53.apk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mViewBinding = MainFirstFragLayoutBinding.bind(((ViewGroup) view).getChildAt(0));

        mViewBinding.viewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TestRecyclerViewRefreshActivity.class);
                startActivity(intent);
            }
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
