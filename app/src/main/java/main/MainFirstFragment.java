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

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainFirstFragment extends BaseFragment {
    public static final String picUrl = "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509119782569&di=1df70625d2ff4e43a03030c5ec480aff&imgtype=0&src=http%3A%2F%2Fwww.th7.cn%2Fd%2Ffile%2Fp%2F2015%2F12%2F31%2Fd08fbed4490f7b6196811abbcc31a209.jpg";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.main_first_frag_layout);
    }

    @Override
    public void onViewCreated(View view) {
        AutoSizeImageView image_1 = (AutoSizeImageView) view.findViewById(R.id.image_1);
        AutoSizeImageView image_2 = (AutoSizeImageView) view.findViewById(R.id.image_2);

        image_1.setWidth(MyUtils.dip2px(200f));
        image_1.setCorner(MyUtils.dip2px(4f));

        image_2.setHeight(MyUtils.dip2px(200f));

        Picasso.with(MyApplication.getApp())
                .load(picUrl)
                .into(image_1);

        Picasso.with(MyApplication.getApp())
                .load(picUrl)
                .into(image_2);

    }

    @Override
    public void onMyBroadcastReceive(String action, Bundle bundle) {
        super.onMyBroadcastReceive(action, bundle);
    }
}
