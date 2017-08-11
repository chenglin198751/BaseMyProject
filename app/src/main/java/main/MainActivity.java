package main;

import android.app.DownloadManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.util.DiffUtil;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import httpwork.HttpUtils;
import listener.OnFinishListener;
import pl.droidsonroids.gif.GifDrawable;
import utils.EasyCache;
import utils.FastBlurUtil;
import utils.MyUtils;
import widget.MyToast;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        getTitleHelper().hideTitle();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMyBroadcastReceive(String myAction, Bundle bundle) {
        super.onMyBroadcastReceive(myAction, bundle);
        if (myAction.equals("1")) {
            Log.v("tag_2", bundle.getString("key"));
        } else if (myAction.equals("2")) {
            Log.v("tag_2", bundle.getString("key"));
        }


    }
}
