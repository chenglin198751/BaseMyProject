package com.wcl.test.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.StringRes;

import com.wcl.test.R;
import com.wcl.test.utils.AppUtils;

public class CommonDialog extends Dialog {

    // ================== View ==================
    private TextView mTitleView;
    private TextView mMessageView;
    private LinearLayout mCustomContainer;
    private View mButtonPanel;
    private TextView mLeftBtn;
    private TextView mRightBtn;

    // ================== State ==================
    private String mTitleText;
    private String mMessageText;
    private View mCustomView;

    private boolean mLeftVisible;
    private boolean mRightVisible;
    private String mLeftText;
    private String mRightText;
    private View.OnClickListener mLeftListener;
    private View.OnClickListener mRightListener;

    public CommonDialog(Context context) {
        this(context, R.style.dialog);
    }

    public CommonDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_alert_dialog);

        AppUtils.setViewRounded(findViewById(R.id.dialog_view), 8);
        AppUtils.setDialogEdgeToEdge(this);

        Window window = getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.getDecorView().setPadding(AppUtils.dp2px(40f), 0, AppUtils.dp2px(40f), 0);
        }
        bindViews();
        applyStateToViews();
    }

    private void bindViews() {
        mTitleView = findViewById(R.id.alertTitle);
        mMessageView = findViewById(R.id.message);
        mCustomContainer = findViewById(R.id.custom_container);
        mButtonPanel = findViewById(R.id.buttonPanel);
        mLeftBtn = findViewById(R.id.button_ok);
        mRightBtn = findViewById(R.id.button_cancel);

        mMessageView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    // ================== Public API ==================

    public void setTitle(String title) {
        mTitleText = title;
        applyTitle();
    }

    public void setTitle(@StringRes int title) {
        setTitle(getContext().getString(title));
    }

    public void setMessage(String msg) {
        mMessageText = msg;
        mCustomView = null;
        applyContent();
    }

    public void setMessage(@StringRes int msg) {
        setMessage(getContext().getString(msg));
    }

    public void setCustomView(View view) {
        mCustomView = view;
        mMessageText = null;
        applyContent();
    }

    public void setLeftButton(String text, View.OnClickListener listener) {
        mLeftVisible = true;
        mLeftText = text;
        mLeftListener = listener;
        applyButtons();
    }

    public void setRightButton(String text, View.OnClickListener listener) {
        mRightVisible = true;
        mRightText = text;
        mRightListener = listener;
        applyButtons();
    }

    // ================== Apply ==================

    private void applyStateToViews() {
        applyTitle();
        applyContent();
        applyButtons();
    }

    private void applyTitle() {
        if (mTitleView == null) return;

        if (TextUtils.isEmpty(mTitleText)) {
            mTitleView.setVisibility(View.GONE);
        } else {
            mTitleView.setText(mTitleText);
            mTitleView.setVisibility(View.VISIBLE);
        }
    }

    private void applyContent() {
        if (mMessageView == null || mCustomContainer == null) return;

        if (mCustomView != null) {
            mMessageView.setVisibility(View.GONE);
            mCustomContainer.removeAllViews();
            mCustomContainer.addView(mCustomView);
            mCustomContainer.setVisibility(View.VISIBLE);
        } else {
            mMessageView.setVisibility(View.VISIBLE);
            mMessageView.setText(mMessageText);
        }
    }

    private void applyButtons() {
        if (mLeftBtn == null || mRightBtn == null || mButtonPanel == null) return;

        mLeftBtn.setVisibility(mLeftVisible ? View.VISIBLE : View.GONE);
        mRightBtn.setVisibility(mRightVisible ? View.VISIBLE : View.GONE);

        if (mLeftVisible) {
            mLeftBtn.setText(mLeftText);
            mLeftBtn.setOnClickListener(mLeftListener);
        }
        if (mRightVisible) {
            mRightBtn.setText(mRightText);
            mRightBtn.setOnClickListener(mRightListener);
        }

        mButtonPanel.setVisibility(
                (mLeftVisible || mRightVisible) ? View.VISIBLE : View.GONE
        );
    }
}
