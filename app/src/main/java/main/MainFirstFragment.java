package main;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import base.BaseApp;
import base.BaseFragment;
import cheerly.mybaseproject.R;
import test.TestAutoScrollAdapter;
import test.TestRecyclerViewRefreshActivity;
import view.AutoScrollRecyclerView;

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
                startActivity(new Intent(getActivity(), TestRecyclerViewRefreshActivity.class));
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
