package main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.IOException;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import httpwork.HttpDownloadCallback;
import httpwork.HttpUtils;
import okhttp3.Call;
import pl.droidsonroids.gif.GifDrawable;
import view.WebImageView;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        startActivity(new Intent(getActivity(),PullDownRefreshActivity.class));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_first_frag_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        String url = "https://w.fengnian.cn/Wallet/download/Android/smallYellowO/aa.zip";
//
//        HttpUtils.downloadFile(url, new HttpDownloadCallback() {
//            @Override
//            public void onFailure(IOException e) {
//                Log.v("tag_2",e.getMessage());
//            }
//
//            @Override
//            public void onSuccess(String filePath) {
//                removeProgress();
//                Log.v("tag_2","size = " + (new File(filePath).length()));
//            }
//
//            @Override
//            public void onStart(String filePath) {
//                showProgress();
//            }
//
//            @Override
//            public void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent) {
//                Log.v("tag_3","percent = " + percent);
//            }
//        });
    }
}
