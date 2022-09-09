package com.wcl.test.utils;

import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
        if (popView != null && popView.getParent() != null) {
            ((ViewGroup) popView.getParent()).removeAllViews();
        }
        if (popView == null) {
            return null;
        }

        PopupWindow popupWindow = null;
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);

        int location[] = new int[2];
        int x, y;
        int popHeight = 0, popWidth = 0;

        attachOnView.getLocationInWindow(location);
        x = location[0];
        y = location[1];

        Integer offsetX = (Integer) popView.getTag(R.id.offset_x);
        Integer offsetY = (Integer) popView.getTag(R.id.offset_y);

        if (offsetX != null) {
            x = x + offsetX;
        }
        if (offsetY != null) {
            y = y + offsetY;
        }

        int h = attachOnView.getHeight();
        int screenHeight = Constants.screenWidth;

        if (popShowHeight == defaultBotom) {
            popHeight = screenHeight / 6;
            popHeight = Math.abs(screenHeight - (h + y)) - popHeight;
        } else if (popHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            popHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        } else {
            popHeight = popShowHeight;
        }

        if (popShowWidth == ViewGroup.LayoutParams.WRAP_CONTENT) {
            popWidth = attachOnView.getWidth();
        } else {
            popWidth = popShowWidth;
        }

        popupWindow = new PopupWindow(popView, popWidth, popHeight, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);

        popupWindow.setAnimationStyle(R.style.PopupAnimationDown);
        popupWindow.showAtLocation(attachOnView, Gravity.NO_GRAVITY, x, h + y);
        popupWindow.update();
        return popupWindow;
    }
}
