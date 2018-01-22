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
    private LongImageView webImageView;
    private Button button;
    private final static String url = "https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnobzhvu6tj30glcmie88.jpg";
    private ArrayList<String> imagesList = new ArrayList<>();
    private int index = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imagesList.add("https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnobzhvu6tj30glcmie88.jpg");
        imagesList.add("https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnoc02i5l2j30ibcmunpj.jpg");
        imagesList.add("https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnobyipj51j30k0c1tx6p.jpg");
        imagesList.add("https://wx4.sinaimg.cn/mw690/006yV7D9gy1fnoc0jlt1gj30g8cmoqvd.jpg");
    }

    private HttpDownloadCallback downloadCallback = new HttpDownloadCallback() {
        @Override
        public void onSuccess(String filePath) {
            getContext().dismissWaitDialog();
        }

        @Override
        public void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent) {

        }

        @Override
        public void onFailure(IOException e) {

        }
    };

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        button = (Button) view.findViewById(R.id.button);
        webImageView = (LongImageView) view.findViewById(R.id.imageView1);
        webImageView.load(imagesList.get(index), Constants.screenWidth);
        index++;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getContext().showWaitDialog("加载中");
                if (index < imagesList.size()) {
                    webImageView.load(imagesList.get(index), Constants.screenWidth, downloadCallback);
                    index++;
                    button.setText("下一页");
                } else {
                    index = 0;
                    webImageView.load(imagesList.get(index), Constants.screenWidth, downloadCallback);
                    button.setText("上一页");
                }

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
