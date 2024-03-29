package com.wcl.test.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.httpwork.HttpUtils;
import com.wcl.test.utils.AppBaseUtils;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {
    private MainFirstFragLayoutBinding mViewBinding;

    //    final String apk_path = "http://qd.shouji.qihucdn.com/media/c30635207cc46df7347d17fe78348c6d/63639203a802f.apk";
    final String apk_path = "http://qd.shouji.qihucdn.com/media/eb1b2f401965fce6f75198d3a5af1299/6305873c18c53.apk";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mViewBinding = MainFirstFragLayoutBinding.bind(((ViewGroup) view).getChildAt(0));

        mViewBinding.viewLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HttpUtils.downloadFile(apk_path, new HttpUtils.HttpDownloadCallback() {
                    @Override
                    public void onFinished(boolean isSuccessful, String filePath, Exception e) {
                        if (isSuccessful) {
                            Log.v("tag_3", "path = " + filePath);
                        }
                    }

                    @Override
                    public void onProgress(long fileTotalSize, long fileDowningSize, float percent) {
                        Log.v("tag_3", "percent1 = " + percent);
                        Log.d("tag_3", "percent2 = " + AppBaseUtils.formatFloat(percent * 100, 2) + "%");
                    }
                });
            }
        });

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
