package main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_first_frag_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String url = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1506077391220&di=908dbbe6a7c605911898ee05722e0738&imgtype=0&src=http%3A%2F%2Fimg.mp.itc.cn%2Fupload%2F20170128%2Fb19e0b30cefb442f9059e372282558cf_th.gif";

        HttpUtils.downloadFile(url, new HttpDownloadCallback() {
            @Override
            public void onFailure(IOException e) {

            }

            @Override
            public void onSuccess(String filePath) {
                removeProgress();
                try {
                    WebImageView webImageView = (WebImageView) getView().findViewById(R.id.image_view);
                    GifDrawable gifFromPath = new GifDrawable(filePath);
                    webImageView.setImageDrawable(gifFromPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onStart(String filePath) {
                showProgress();
            }

            @Override
            public void onProgress(Call call, long fileTotalSize, long fileDowningSize, int percent) {

            }
        });
    }
}
