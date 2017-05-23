package main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import base.BaseActivity;
import cheerly.mybaseproject.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMyBroadcast(new Intent().putExtra("a", "b"));
            }
        });

    }

    @Override
    public void onMyBroadcastReceive(Context context, Intent intent) {
        super.onMyBroadcastReceive(context, intent);
        Log.v("tag_2", intent.getAction());
        Log.v("tag_2", intent.getStringExtra("a"));

    }
}
