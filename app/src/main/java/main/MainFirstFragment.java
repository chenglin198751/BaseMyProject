package main;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import test.TestAutoScrollAdapter;
import view.AutoScrollRecyclerView;

/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String mWebViewUrl = "你好https://WX.tenpay.com/cgi-bin/mmpayweb";
        Log.v("tag_2", "" + mWebViewUrl.toLowerCase());

    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        initView(view);
    }

    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        super.onBroadcastReceiver(action, bundle);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }


    private void initView(View view) {
        AutoScrollRecyclerView mRecyclerView = view.findViewById(R.id.rv_recycleView);
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add("今天天气不错" + i);
        }
        TestAutoScrollAdapter adapter = new TestAutoScrollAdapter(getActivity(), list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.start();
    }
}
