package main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import test.TestRecyclerViewRefreshActivity;
import utils.MyUtils;
import view.CenterDrawable;
import view.WebImageView;
import widget.MyDialog;
import widget.MyWebViewActivity;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    private WebImageView webImageView;
    private final static String url = "http://5b0988e595225.cdn.sohucs.com/images/20170922/c7e95cf930a64a27b616e8c77525645b.jpeg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        webImageView = (WebImageView) view.findViewById(R.id.imageView1);
        webImageView.load(url, -1,-1);

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(),TestRecyclerViewRefreshActivity.class));
            }
        });
    }

    @Override
    public void onMyBroadcastReceiver(String action, Bundle bundle) {
        super.onMyBroadcastReceiver(action, bundle);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }
}
