package main;

import android.os.Bundle;

import base.BaseActivity;
import cheerly.mybaseproject.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.activity_main);
        showProgress();

    }
}
