package main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import base.BaseActivity;
import cheerly.mybaseproject.R;
import widget.MyDialog;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);

        getTitleHelper().setTitle("测试");

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog myDialog = new MyDialog(MainActivity.this);
                myDialog.setMessage("我是内容");
                myDialog.setTitle("我是标题");
                myDialog.setLeftButton("删除",new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });

                myDialog.setRightButton("确定", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                myDialog.show();
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
