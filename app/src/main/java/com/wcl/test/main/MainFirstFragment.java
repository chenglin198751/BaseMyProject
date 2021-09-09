package com.wcl.test.main;

import android.content.Intent;
import android.os.Bundle;

import android.view.View;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.test.TestRecyclerViewRefreshActivity;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {

        view.findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TestRecyclerViewRefreshActivity.class);
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
