package main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import widget.MyWebViewActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        getTitleHelper().hideTitle();

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MyWebViewActivity.class));
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
