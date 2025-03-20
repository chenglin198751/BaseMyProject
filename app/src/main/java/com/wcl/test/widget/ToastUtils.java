package com.wcl.test.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wcl.test.base.BaseApp;
import com.wcl.test.R;
import com.wcl.test.utils.AppBaseUtils;


/**
 * Created by chenglin on 2017-7-24.
 */

public class ToastUtils {

    public static void show(int resId) {
        show(BaseApp.getApp().getString(resId));
    }

    public static void show(final String text) {
        if (TextUtils.isEmpty(text)){
            return;
        }

        if (AppBaseUtils.isUiThread()) {
            showToast(text);
        } else {
            AppBaseUtils.getUiHandler().post(new Runnable() {
                @Override
                public void run() {
                    showToast(text);
                }
            });
        }
    }

    private static void showToast(String message) {
        LayoutInflater inflater = (LayoutInflater) BaseApp.getApp().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.my_toast_layout, null);
        TextView textView = layout.findViewById(R.id.message);
        textView.setText(message);
        Toast toast = new Toast(BaseApp.getApp());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 50);
        toast.show();
    }

}
