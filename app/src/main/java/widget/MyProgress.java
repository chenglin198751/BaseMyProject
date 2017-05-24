package widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import cheerly.mybaseproject.R;


public class MyProgress {
    private static MyDialog dialog;
    private static View dialogView;

    /**
     * 显示一个等待对话框
     */
    public static void showProgress(Context context, String text) {
        dialog = new MyDialog(context);
        dialogView = View.inflate(context, R.layout.progress_layout, null);
        dialog.setFullScreenView(dialogView);
        if (!TextUtils.isEmpty(text)) {
            setText(text);
        }
        dialog.show();
    }

    /**
     * 显示一个等待对话框
     */
    public static void showProgress(Context context) {
        showProgress(context, null);
    }

    /**
     * 显示一个等待对话框
     */
    public static void showProgress(Context context, int textResId) {
        showProgress(context, context.getString(textResId));
    }

    /**
     * 设置上面的文字
     */
    public static void setText(String text) {
        TextView textView = (TextView) dialogView.findViewById(R.id.text);
        textView.setText(text);
    }

    /**
     * 设置上面的文字
     */
    public static void setText(int text) {
        TextView textView = (TextView) dialogView.findViewById(R.id.text);
        textView.setText(text);
    }

    /**
     * 按返回键是否取消进度框
     */
    public static void setCancelable(boolean flag) {
        dialog.setCancelable(flag);
    }

    /**
     * 取消对话框
     */
    public static void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
            dialogView = null;
        }
    }
}
