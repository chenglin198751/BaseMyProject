package widget;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import base.BaseApp;
import cheerly.mybaseproject.R;
import utils.BaseUtils;


/**
 * Created by chenglin on 2017-7-24.
 */

public class ToastUtils {
    private static Toast mToast;

    public static void show(int resId) {
        show(BaseApp.getApp().getString(resId));
    }

    public static void show(final String text) {
        if (BaseUtils.isUiThread()) {
            showToast(text);
        } else {
            BaseUtils.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    showToast(text);
                }
            });
        }
    }

    private static void showToast(String text) {
        TextView tvMessage;
        if (mToast == null) {
            mToast = new Toast(BaseApp.getApp());

            View view = View.inflate(BaseApp.getApp(), R.layout.my_toast_layout, null);
            tvMessage = view.findViewById(R.id.message);
            tvMessage.setText(text);

            mToast.setView(view);
            mToast.setDuration(Toast.LENGTH_SHORT);
        } else {
            tvMessage = mToast.getView().findViewById(R.id.message);
            tvMessage.setText(text);
        }
        mToast.show();
    }

}
