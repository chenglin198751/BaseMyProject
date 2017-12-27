package main;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.squareup.picasso.Picasso;

import base.BaseFragment;
import base.MyApplication;
import cheerly.mybaseproject.R;
import utils.MyUri;
import utils.MyUtils;
import view.AutoSizeImageView;
import view.WebImageView;
import widget.MyWebViewActivity;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.main_first_frag_layout);

    }

    @Override
    protected void onViewCreated(View view) {
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://wxpay.wxutil.com/mch/pay/h5.v2.php";
                MyWebViewActivity.start(getContext(),url,"微信支付测试");
            }
        });
    }

    @Override
    public void onMyBroadcastReceive(String action, Bundle bundle) {
        super.onMyBroadcastReceive(action, bundle);
    }
}
