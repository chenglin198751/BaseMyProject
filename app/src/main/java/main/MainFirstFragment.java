package main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;

import base.BaseFragment;
import base.MyApplication;
import cheerly.mybaseproject.R;
import utils.MyUri;
import utils.MyUtils;
import view.AutoSizeImageView;
import view.WebImageView;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    public static final String picUrl = "http://smallyellow.tinydonuts.cn/5780f0e8f5264d62a4f3e1af209123a2.png";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.main_first_frag_layout);

    }

    @Override
    protected void onViewCreated(View view) {
        showProgress(null);
        WebImageView image_1 = (WebImageView) view.findViewById(R.id.image_1);
        image_1.load(picUrl);
    }

    @Override
    public void onMyBroadcastReceive(String action, Bundle bundle) {
        super.onMyBroadcastReceive(action, bundle);
    }
}
