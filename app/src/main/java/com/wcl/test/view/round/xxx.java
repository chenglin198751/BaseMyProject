package com.wcl.test.view.round;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;

import com.wcl.test.utils.AppBaseUtils;

public class xxx {
    public static GradientDrawable createRoundedRectangleDrawable(
            Context context,
            float cornerRadiusDp,
            int solidColor,
            int strokeColor,
            float strokeWidthDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        // 设置圆角半径
        float cornerRadiusPx = AppBaseUtils.dp2px(cornerRadiusDp);
        drawable.setCornerRadius(cornerRadiusPx);

        // 设置填充颜色
        drawable.setColor(solidColor);

        // 设置描边颜色和宽度
        float strokeWidthPx = AppBaseUtils.dp2px(strokeWidthDp);
        drawable.setStroke((int) strokeWidthPx, strokeColor);

        return drawable;
    }
}
