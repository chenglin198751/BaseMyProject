package main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import test.TestRecyclerViewRefreshActivity;
import utils.Constants;
import utils.MyUtils;
import widget.LongImageView;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    private LongImageView imageView;
    private final static String url = "https://b-ssl.duitang.com/uploads/item/201409/20/20140920230643_8tij8.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        imageView = (LongImageView) view.findViewById(R.id.image_1);
        imageView.load(url, Constants.screenWidth);

        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), TestRecyclerViewRefreshActivity.class));

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
