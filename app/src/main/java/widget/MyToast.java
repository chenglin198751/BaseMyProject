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
        TextView tvMessage;
        if (mToast == null) {
            mToast = new Toast(MyApp.getApp());

            View view = View.inflate(MyApp.getApp(), R.layout.my_toast_layout, null);
            tvMessage = (TextView) view.findViewById(R.id.message);
            tvMessage.setText(text);

            mToast.setView(view);
            mToast.setDuration(Toast.LENGTH_SHORT);
        } else {
            tvMessage = (TextView) mToast.getView().findViewById(R.id.message);
            tvMessage.setText(text);
        }
        mToast.show();
    }

}
