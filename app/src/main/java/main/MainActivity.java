package main;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import junit.framework.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import httpwork.BaseModel;
import httpwork.HttpCallback;
import httpwork.HttpUtils;
import listener.MyCallback;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import utils.BitmapUtils;
import utils.Constants;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);

        getTitleHelper().setTitle("测试");

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("key", "7bc922a36370604b885df9eb15a7b147");
                map.put("location", "116.481488,39.990464");
                map.put("keywords", "超市");

                String url = "http://restapi.amap.com/v3/place/around";

                HttpUtils.post(MainActivity.this, url, map, new HttpCallback() {
                    @Override
                    public void onFailure(IOException e) {

                    }

                    @Override
                    public void onSuccess(String result) {
                        Log.v("tag_2", result);
                    }
                });


            }
        });

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
