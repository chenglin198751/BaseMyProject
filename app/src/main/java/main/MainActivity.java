package main;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
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
import pl.droidsonroids.gif.GifDrawable;
import utils.EasyCache;
import widget.MyToast;

public class MainActivity extends BaseActivity {
    private EditText mEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyToast.show("今天天气很好啊");

//                MyToast.showToast(MainActivity.this,"今天天气很好啊");
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
