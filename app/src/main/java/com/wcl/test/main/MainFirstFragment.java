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
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.httpwork.HttpUtils;
import com.wcl.test.test.TestRecyclerViewRefreshActivity;
import com.wcl.test.utils.BaseUtils;
import com.wcl.test.utils.FileUtils;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.widget.BaseWebViewActivity;

import java.io.File;
import java.io.IOException;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import okhttp3.Call;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {


    final String apk_path = "http://qd.shouji.qihucdn.com/media/eb1b2f401965fce6f75198d3a5af1299/6305873c18c53.apk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {

        view.findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                HttpUtils.downloadFile(apk_path,FileUtils.getExternalDownloadPath() + "/dd.apk",true, new HttpUtils.HttpDownloadCallback() {
                HttpUtils.downloadFile(apk_path, new HttpUtils.HttpDownloadCallback() {
                    @Override
                    public void onSuccess(String filePath) {
                        Log.v("tag_99","filePath111 = " + filePath);
                        BaseUtils.installApk(getContext(),filePath);
                    }

                    @Override
                    public void onProgress(Call call, long fileTotalSize, long fileDowningSize, float percent) {
                        Log.v("tag_99","percent = " + percent);
                    }

                    @Override
                    public void onFailure(IOException e) {
                        Log.v("tag_99","onFailure = " + e);
                    }
                });

                String path = FileUtils.getExternalDownloadPath();

                Log.v("tag_99",path);
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
