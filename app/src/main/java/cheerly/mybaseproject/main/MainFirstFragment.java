package cheerly.mybaseproject.main;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import cheerly.mybaseproject.base.BaseFragment;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.httpwork.HttpUtils;
import cheerly.mybaseproject.widget.BaseWebViewActivity;
import cheerly.mybaseproject.widget.ToastUtils;

/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {

        view.findViewById(R.id.button_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                getData();
                BaseWebViewActivity.start(getContext(),"https://www.baidu.com/","百度");
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

    private void getData(){
        String url = "http://wanandroid.com/wxarticle/chapters/json";
        HttpUtils.get(getContext(), url, new HttpUtils.HttpCallback() {
            @Override
            public void onSuccess(String result) {
                Log.v("tag_3","result = " + result);
            }

            @Override
            public void onFailure(HttpUtils.HttpException e) {
                ToastUtils.show(e.errorMsg);
            }
        });
    }

}
