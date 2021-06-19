package com.wcl.test.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.wcl.test.R;
import com.wcl.test.utils.BaseUtils;


public class CommonDialog extends Dialog {
    private View mDialogView = null;
    private boolean isLeftVisible = false;
    private boolean isRightVisible = false;
    private TextView mLeftBtn, mRightBtn;
    private TextView mMessage;


    public CommonDialog(Context context) {
        this(context, R.style.dialog);
        mDialogView = View.inflate(context, R.layout.common_alert_dialog, null);
        mDialogView.findViewById(R.id.title_template).setVisibility(View.GONE);
        mLeftBtn = mDialogView.findViewById(R.id.button_ok);
        mRightBtn = mDialogView.findViewById(R.id.button_cancel);
        mMessage = mDialogView.findViewById(R.id.message);
        mMessage.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    protected CommonDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(mDialogView);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().getDecorView().setPadding(BaseUtils.dip2px(45f), 0, BaseUtils.dip2px(45f), 0);
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override
    public void show() {
        super.show();

        if (isLeftVisible) {
            mLeftBtn.setVisibility(View.VISIBLE);
        } else {
            mLeftBtn.setVisibility(View.GONE);
        }
        if (isRightVisible) {
            mRightBtn.setVisibility(View.VISIBLE);
        } else {
            mRightBtn.setVisibility(View.GONE);
        }

        if (!isLeftVisible && !isRightVisible) {
            mDialogView.findViewById(R.id.buttonPanel).setVisibility(View.GONE);
            mDialogView.findViewById(R.id.bottom_line).setVisibility(View.INVISIBLE);
        } else {
            mDialogView.findViewById(R.id.buttonPanel).setVisibility(View.VISIBLE);
            mDialogView.findViewById(R.id.bottom_line).setVisibility(View.VISIBLE);
        }

    }


    /**
     * 设置自定义View
     */
    public void setCustomView(View view) {
        if (view == null) {
            return;
        }
        mDialogView.findViewById(R.id.message).setVisibility(View.GONE);

        LinearLayout customLinear = mDialogView.findViewById(R.id.my_custom);
        customLinear.addView(view);
    }

    /**
     * 为左边的按钮设置点击事件，并设置文字
     */
    public void setLeftButton(String text, View.OnClickListener listener) {
        if (text != null) {
            mLeftBtn.setText(text);
        }
        mLeftBtn.setOnClickListener(listener);
        isLeftVisible = true;
    }

    /**
     * 为右边的按钮设置点击事件，并设置文字
     */
    public void setRightButton(String text, View.OnClickListener listener) {
        if (text != null) {
            mRightBtn.setText(text);
        }
        mRightBtn.setOnClickListener(listener);
        isRightVisible = true;
    }

    /**
     * 为对话框设置一个短消息显示
     */
    public void setMessage(String msg) {
        mMessage.setText(msg);
    }

    /**
     * 为对话框设置一个短消息显示
     */
    public void setMessage(@StringRes int msg) {
        setMessage(getContext().getString(msg));
    }

    public void setTitle(String title) {
        mDialogView.findViewById(R.id.title_template).setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(title)) {
            TextView view = mDialogView.findViewById(R.id.alertTitle);
            view.setText(title);
        }
    }

    public void setTitle(@StringRes int title) {
        setTitle(getContext().getString(title));
    }


}
