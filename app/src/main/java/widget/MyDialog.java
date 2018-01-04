package widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cheerly.mybaseproject.R;
import utils.MyUtils;


public class MyDialog extends Dialog {
    private View mDialogView = null;
    private boolean isLeftVisible = false;
    private boolean isRightVisible = false;
    private Button mLeftBtn, mRightBtn;
    private TextView mMessage;

    //让对话框左边的按钮高亮
    public static final int LEFT_LIGHT = 1;
    //让对话框右边的按钮高亮
    public static final int RIGHT_LIGHT = 2;

    public MyDialog(Context context) {
        this(context, R.style.dialog);
        mDialogView = View.inflate(context, R.layout.alert_dialog, null);
        mDialogView.findViewById(R.id.title_template).setVisibility(View.GONE);
        mLeftBtn = (Button) mDialogView.findViewById(R.id.button_ok);
        mRightBtn = (Button) mDialogView.findViewById(R.id.button_cancel);
        mMessage = (TextView) mDialogView.findViewById(R.id.message);
    }

    protected MyDialog(Context context, int theme) {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        this.setContentView(mDialogView);
        setScrollViewHeight();
    }

    /**
     * 当文字区域很大时，就固定文字区域外层的ScrollView的高度
     */
    private void setScrollViewHeight() {
        mMessage.post(new Runnable() {
            @Override
            public void run() {
                ScrollView scrollView = (ScrollView) mDialogView.findViewById(R.id.scrollView);
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) scrollView.getLayoutParams();
                if (mMessage.getHeight() > MyUtils.dip2px(330f)) {
                    params.height = MyUtils.dip2px(330f);
                    scrollView.setLayoutParams(params);
                } else {
                    if (params.height != ViewGroup.LayoutParams.WRAP_CONTENT) {
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        scrollView.setLayoutParams(params);
                    }
                }
            }
        });
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
     * 为左边的按钮设置点击事件
     */
    public void setLeftButton(View.OnClickListener listener) {
        setLeftButton(null, listener);
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
     * 为右边的按钮设置点击事件
     */
    public void setRightButton(View.OnClickListener listener) {
        setRightButton(null, listener);
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
        if (!TextUtils.isEmpty(title)) {
            mDialogView.findViewById(R.id.title_template).setVisibility(View.VISIBLE);
            TextView view = (TextView) mDialogView.findViewById(R.id.alertTitle);
            view.setText(title);
        }
    }

    public void setTitle(@StringRes int title) {
        setTitle(getContext().getString(title));
    }

//    /**
//     * 设置要高亮显示的按钮
//     */
//    public void setButtonLight(int lightBtn) {
//        Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
//        Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
//        if (lightBtn == LEFT_LIGHT) {
//            leftButton.setBackgroundResource(R.drawable.purple_button);
//            leftButton.setTextColor(mContext.getResources().getColor(R.color.white));
//            rightButton.setBackgroundResource(R.drawable.gray_button);
//            rightButton.setTextColor(mContext.getResources().getColor(R.color.text_gray_2));
//        } else if (lightBtn == RIGHT_LIGHT) {
//            leftButton.setBackgroundResource(R.drawable.gray_button);
//            leftButton.setTextColor(mContext.getResources().getColor(R.color.text_gray_2));
//            rightButton.setBackgroundResource(R.drawable.purple_button);
//            rightButton.setTextColor(mContext.getResources().getColor(R.color.white));
//        }
//    }

//    /**
//     * 设置两个按钮都高亮
//     */
//    public void setButtonLight() {
//        Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
//        Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
//        leftButton.setBackgroundResource(R.drawable.purple_button);
//        leftButton.setTextColor(mContext.getResources().getColor(R.color.white));
//        rightButton.setBackgroundResource(R.drawable.purple_button);
//        rightButton.setTextColor(mContext.getResources().getColor(R.color.white));
//    }

}
