package com.wcl.test.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.wcl.test.R;

/**
 * weichenglin create in 15/5/12
 */
public class PopupWindowUtils {
    public static final int defaultBotom = -100;

    /**
     * 让popView显示在attachOnView的下面，popShowHeight 和 popShowWidth 的单位都是像素
     * popView.setTag(R.id.offset_x,px) 和 popView.setTag(R.id.offset_y,px) 可以设置当前显示popWindow的坐标偏移量
     */
    public static PopupWindow show(Activity activity, View attachOnView, View popView, final int popShowHeight, final int popShowWidth) {
        if (activity == null || attachOnView == null) {
            return null;
        }

        if (popView != null && popView.getParent() instanceof ViewGroup) {
            ((ViewGroup) popView.getParent()).removeAllViews();
        }

        if (popView == null) {
            return null;
        }

        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        int[] location = new int[2];
        attachOnView.getLocationInWindow(location);
        int x = location[0];
        int y = location[1];

        Integer offsetX = (Integer) popView.getTag(R.id.offset_x);
        Integer offsetY = (Integer) popView.getTag(R.id.offset_y);

        if (offsetX != null) {
            x += offsetX;
        }
        if (offsetY != null) {
            y += offsetY;
        }

        int h = attachOnView.getHeight();

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        int screenHeight = metrics.heightPixels;

        int popHeight;
        if (popShowHeight == defaultBotom) {
            // 默认 bottom 高度为屏幕剩余空间减去 1/6 屏幕高度
            popHeight = Math.abs(screenHeight - (h + y)) - screenHeight / 6;
        } else if (popShowHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            popHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            popHeight = popShowHeight;
        }

        int popWidth;
        if (popShowWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            popWidth = attachOnView.getWidth();
        } else {
            popWidth = popShowWidth;
        }

        PopupWindow popupWindow = new PopupWindow(popView, popWidth, popHeight, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.PopupAnimationDown);
        popupWindow.showAtLocation(attachOnView, Gravity.NO_GRAVITY, x, h + y);
        popupWindow.update();

        return popupWindow;
    }
}
