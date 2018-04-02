package main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.os.Bundle;
import android.support.v4.graphics.BitmapCompat;
import android.view.View;
import android.widget.Button;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import photo.SelectPhotosActivity;
import test.TestActivity;
import test.TestRecyclerViewRefreshActivity;
import utils.BitmapUtils;
import utils.MyUtils;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivity(new Intent(getContext(), TestRecyclerViewRefreshActivity.class));

                showWaitDialog("正在加载数据");
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
