package com.wcl.test.main;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;

import com.wcl.test.R;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.MainFirstFragLayoutBinding;
import com.wcl.test.utils.SmartImageLoader;
import com.wcl.test.widget.CommonDialog;


/**
 * Created by chenglin on 2017-9-14.
 */
public class MainFirstFragment extends BaseFragment {
    private MainFirstFragLayoutBinding mViewBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onViewCreated(Bundle savedInstanceState, View view) {
        mViewBinding = MainFirstFragLayoutBinding.bind(((ViewGroup) view).getChildAt(0));

        mViewBinding.viewLeft.setOnClickListener(v -> {
            CommonDialog dialog = new CommonDialog(getContext());
            dialog.setTitle("警告");
            dialog.setMessage("要过年了吗");
            dialog.setLeftButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            dialog.setRightButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            dialog.show();
        });

        String url = "http://qd.shouji.qihucdn.com/media/d22eee36c269dcae8dbfc6a469d02ffc/6602326c507c2.png";
        SmartImageLoader.load(mViewBinding.image2,url,-1,-1,0);
    }

    @Override
    public void onEvent(String eventKey, Object data) {
        super.onEvent(eventKey, data);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_first_frag_layout;
    }


}
