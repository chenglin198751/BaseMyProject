package widget;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import base.MyApp;
import cheerly.mybaseproject.R;


/**
 * Created by chenglin on 2017-7-24.
 */

public class MyToast {
    private static Toast mToast;

    public static void show(int resId) {
        show(MyApp.getApp().getString(resId));
    }

    public static void show(String text) {
        TextView msgTv = null;
        if (mToast == null) {
            mToast = new Toast(MyApp.getApp());

            View view = View.inflate(MyApp.getApp(), R.layout.my_toast_layout, null);
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
