package com.wcl.test.main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.Transformation;
import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.test.TestRecyclerViewRefreshActivity;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.SmartImageLoader;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;


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
                Log.v("tag_99","11");
            }
        });

        final String url = "http://sjbz.fd.zol-img.com.cn/t_s1080x1920c/g7/M00/03/03/ChMkK2IhwkuIQrMuAA44okS-NnMAABM7gD2hrgADji6794.jpg";


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
