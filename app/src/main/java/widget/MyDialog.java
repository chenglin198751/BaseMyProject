package widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cheerly.mybaseproject.R;
import utils.MyUtils;


public class MyDialog extends Dialog {
    private Context mContext;
    private View mDialogView = null;
    private int mLeftBtn = 0, mRightBtn = 0;
    private boolean OnlySetAView = false;

    /**
     * 让对话框左边的按钮高亮
     */
    public static final int LEFT_LIGHT = 1;
    /**
     * 让对话框右边的按钮高亮
     */
    public static final int RIGHT_LIGHT = 2;

    public MyDialog(Context context) {
        this(context, R.style.dialog);
        this.mContext = context;
        mDialogView = View.inflate(context, R.layout.alert_dialog, null);
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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!hasFocus) {
            return;
        }

        RelativeLayout titleView = (RelativeLayout) mDialogView.findViewById(R.id.title_template);
        LinearLayout bottomView = (LinearLayout) mDialogView.findViewById(R.id.buttonPanel);
        LinearLayout myCustom = (LinearLayout) mDialogView.findViewById(R.id.my_custom);
        LinearLayout dialogView = (LinearLayout) mDialogView.findViewById(R.id.dialog_view);

        int height = dialogView.getHeight();
        int titleHeight = titleView.getHeight();
        int bottomHeight = bottomView.getHeight();
        int customHeight = myCustom.getHeight();

        if (height - (titleHeight + bottomHeight) < customHeight) {
            LinearLayout.LayoutParams params = (LayoutParams) myCustom.getLayoutParams();
            params.height = height - (titleHeight + bottomHeight);
            myCustom.setLayoutParams(params);
        }
    }

    @Override
    public void show() {
        super.show();

        if (!OnlySetAView) {
            if (mLeftBtn == 0) {
                mDialogView.findViewById(R.id.button_ok).setVisibility(View.GONE);
            }
            if (mRightBtn == 0) {
                mDialogView.findViewById(R.id.button_cancel).setVisibility(View.GONE);
            }

            if (mLeftBtn + mRightBtn == 0) {
                mDialogView.findViewById(R.id.buttonPanel).setVisibility(View.GONE);
            }
            this.setContentView(mDialogView);
        }
    }

    /**
     * 设置充满屏幕的VIEW
     */
    public void setFullScreenView(View view) {
        this.OnlySetAView = true;
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        this.setContentView(view, new LinearLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT));
    }


    /**
     * 为自定义的对话框设置内容布局
     */
    public void setView(View view) {
        LinearLayout myView = (LinearLayout) mDialogView.findViewById(R.id.my_custom);
        myView.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        int _PX_10 = MyUtils.dip2px(10f);
        params.gravity = Gravity.CENTER;
        params.setMargins(_PX_10 * 2, _PX_10, _PX_10 * 2, 0);
        myView.addView(view, params);
    }

    /**
     * 为左边的按钮设置点击事件，并设置文字
     */
    public void setLeftButton(String text, View.OnClickListener LeftButtonClickListener) {
        if (text != null) {
            Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
            leftButton.setText(text);
        }
        setLeftButton(LeftButtonClickListener);
    }

    /**
     * 为左边的按钮设置点击事件
     */
    public void setLeftButton(View.OnClickListener LeftButtonClickListener) {
        Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
        leftButton.setVisibility(View.VISIBLE);
        leftButton.setOnClickListener(LeftButtonClickListener);
        mLeftBtn = 1;
    }

    ;

    /**
     * 为右边的按钮设置点击事件，并设置文字
     */
    public void setRightButton(String text, View.OnClickListener RightButtonClickListener) {
        if (text != null) {
            Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
            rightButton.setText(text);
        }
        setRightButton(RightButtonClickListener);
    }

    /**
     * 为右边的按钮设置点击事件
     */
    public void setRightButton(View.OnClickListener RightButtonClickListener) {
        Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(RightButtonClickListener);
        mRightBtn = 1;
    }

    /**
     * 为对话框设置一个短消息显示
     */
    public void setMessage(String msg) {
        TextView view = (TextView) mDialogView.findViewById(R.id.message);
        view.setText(msg);
    }

    /**
     * 为对话框设置一个短消息显示
     */
    public void setMessage(int msg) {
        TextView view = (TextView) mDialogView.findViewById(R.id.message);
        view.setText(msg);
    }

    public void setTitle(String title) {
        TextView view = (TextView) mDialogView.findViewById(R.id.alertTitle);
        view.setText(title);
    }

    public void setTitle(int title) {
        TextView view = (TextView) mDialogView.findViewById(R.id.alertTitle);
        view.setText(title);
    }

    /**
     * 设置要高亮显示的按钮
     */
    public void setButtonLight(int lightBtn) {
        Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
        Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
        if (lightBtn == LEFT_LIGHT) {
            leftButton.setBackgroundResource(R.drawable.purple_button);
            leftButton.setTextColor(mContext.getResources().getColor(R.color.text_color_white));
            rightButton.setBackgroundResource(R.drawable.gray_button);
            rightButton.setTextColor(mContext.getResources().getColor(R.color.text_gray_2));
        } else if (lightBtn == RIGHT_LIGHT) {
            leftButton.setBackgroundResource(R.drawable.gray_button);
            leftButton.setTextColor(mContext.getResources().getColor(R.color.text_gray_2));
            rightButton.setBackgroundResource(R.drawable.purple_button);
            rightButton.setTextColor(mContext.getResources().getColor(R.color.text_color_white));
        }
    }

    /**
     * 设置两个按钮都高亮
     */
    public void setButtonLight() {
        Button leftButton = (Button) mDialogView.findViewById(R.id.button_ok);
        Button rightButton = (Button) mDialogView.findViewById(R.id.button_cancel);
        leftButton.setBackgroundResource(R.drawable.purple_button);
        leftButton.setTextColor(mContext.getResources().getColor(R.color.text_color_white));
        rightButton.setBackgroundResource(R.drawable.purple_button);
        rightButton.setTextColor(mContext.getResources().getColor(R.color.text_color_white));
    }

}
