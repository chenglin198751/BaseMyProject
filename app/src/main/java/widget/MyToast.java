package widget;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import base.MyApplication;
import cheerly.mybaseproject.R;


/**
 * Created by chenglin on 2017-7-24.
 */

public class MyToast {
    private static Toast mToast;

    public static void show(String text) {
        TextView msgTv = null;
        if (mToast == null) {
            mToast = new Toast(MyApplication.getApp());

            View view = View.inflate(MyApplication.getApp(), R.layout.my_toast_layout, null);
            msgTv = (TextView) view.findViewById(R.id.message);
            msgTv.setText(text);

            mToast.setView(view);
            mToast.setDuration(Toast.LENGTH_SHORT);
        } else {
            msgTv = (TextView) mToast.getView().findViewById(R.id.message);
            msgTv.setText(text);
        }
        mToast.show();
    }

}
