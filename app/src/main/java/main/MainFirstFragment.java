package main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.UUID;

import base.BaseFragment;
import cheerly.mybaseproject.R;
import utils.DESUtils;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    String aa;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        view.findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aa = DESUtils.encrypt("今天天气真好，一起去郊游吧", "abcdabcd");
                Log.v("tag_2", "加密 = " + aa);
            }
        });

        view.findViewById(R.id.button_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.v("tag_2", "解密 = " + DESUtils.decrypt(aa, "abcdabcd"));

                Log.v("tag_2", "解密 = " + UUID.randomUUID());

            }
        });

    }

    @Override
    public void onBroadcastReceiver(String action, Bundle bundle) {
        super.onBroadcastReceiver(action, bundle);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }
}
