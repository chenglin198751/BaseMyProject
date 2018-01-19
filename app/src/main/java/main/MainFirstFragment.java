package main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import test.TestRecyclerViewRefreshActivity;
import view.WebImageView;
import widget.MyDialog;
import widget.MyWebViewActivity;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    private WebImageView webImageView;
    private final static String url = "http://img.zcool.cn/community/01bd32573bd4c432f8757cb9341633.gif";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        webImageView = (WebImageView) view.findViewById(R.id.image_1);
        webImageView.load(url, -1, -1);

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
