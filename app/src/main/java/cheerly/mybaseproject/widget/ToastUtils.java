package cheerly.mybaseproject.widget;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import cheerly.mybaseproject.base.BaseApp;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;


/**
 * Created by chenglin on 2017-7-24.
 */

public class ToastUtils {

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
        Toast toast = new Toast(BaseApp.getApp());

        View view = View.inflate(BaseApp.getApp(), R.layout.my_toast_layout, null);
        tvMessage = view.findViewById(R.id.message);
        tvMessage.setText(text);
        toast.setView(view);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}
