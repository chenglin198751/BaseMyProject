package main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.IOException;
import java.util.ArrayList;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import httpwork.HttpDownloadCallback;
import okhttp3.Call;
import utils.Constants;
import widget.LongImageView;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    private Button button;
    private final static String url = "https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnobzhvu6tj30glcmie88.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        button = (Button) view.findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


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
