package main;

import android.os.Bundle;
import android.view.View;

import base.BaseFragment;
import cheerly.mybaseproject.R;
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
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        view.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "http://wxpay.wxutil.com/mch/pay/h5.v2.php";
                MyWebViewActivity.start(getContext(), url, "微信支付测试");
            }
        });
    }

    @Override
    public void onMyBroadcastReceiver(String action, Bundle bundle) {
        super.onMyBroadcastReceiver(action, bundle);
    }
}
