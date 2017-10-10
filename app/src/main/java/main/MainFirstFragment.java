package main;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import utils.MyUri;

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
    public void onViewCreated(View view) {
        final Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyUri.start(getContext(), "mls://webview?title=测试&url=http://news.qq.com/");
            }
        });

        view.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideProgress();
            }
        });
    }

    @Override
    public void onMyBroadcastReceive(String action, Bundle bundle) {
        super.onMyBroadcastReceive(action, bundle);
    }
}
