package com.wcl.test.view.round;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;
import com.wcl.test.utils.AppBaseUtils;

public class RoundedImageView extends AppCompatImageView {

    private float cornerRadius = 0f;
    private boolean isOval = false;
    private float aspectRatio = 0f;
    private float borderWidth = 0f;
    private int borderColor = Color.TRANSPARENT;
    private int solidColor = Color.TRANSPARENT;

    private GradientDrawable gradientDrawable;

    public RoundedImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
            cornerRadius = a.getDimension(R.styleable.RoundedImageView_riv_corner_radius, 0f);
            isOval = a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);
            aspectRatio = a.getFloat(R.styleable.RoundedImageView_riv_aspect_ratio, 0f);
            borderWidth = a.getDimension(R.styleable.RoundedImageView_riv_border_width, 0f);
            borderColor = a.getColor(R.styleable.RoundedImageView_riv_border_color, Color.TRANSPARENT);
            solidColor = a.getColor(R.styleable.RoundedImageView_riv_solid_color, Color.TRANSPARENT);
            a.recycle();

            if (gradientDrawable == null) {
                gradientDrawable = createRoundedRectangleDrawable(cornerRadius, solidColor, borderColor, borderWidth);
            } else {
                gradientDrawable.setCornerRadius(cornerRadius);
            }
            setBackground(gradientDrawable);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (aspectRatio > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (width == 0 && height != 0) {
                width = (int) (height * aspectRatio);
            } else if (height == 0 && width != 0) {
                height = (int) (width / aspectRatio);
            }
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    /**
     * 创建带圆角、填充色和描边的矩形 GradientDrawable
     *
     * @param cornerRadius 圆角半径
     * @param solidColor     矩形填充色
     * @param strokeColor    描边颜色
     * @param strokeWidth  描边宽度
     * @return GradientDrawable
     */
    private GradientDrawable createRoundedRectangleDrawable(
            float cornerRadius,
            int solidColor,
            int strokeColor,
            float strokeWidth) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);

        // 设置圆角半径
        drawable.setCornerRadius(cornerRadius);

        // 设置填充颜色
        drawable.setColor(solidColor);

        // 设置描边颜色和宽度
        drawable.setStroke((int) strokeWidth, strokeColor);

        return drawable;
    }

    // ---------------- Public API ----------------

    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        if (gradientDrawable == null) {
            gradientDrawable = createRoundedRectangleDrawable(cornerRadius, solidColor, borderColor, borderWidth);
        } else {
            gradientDrawable.setCornerRadius(cornerRadius);
        }
        setBackground(gradientDrawable);
    }

    public void setOval(boolean oval) {
        isOval = oval;

        // 设置 1000f 用来实现让GradientDrawable是纯圆形展示
        if (gradientDrawable == null) {
            gradientDrawable = createRoundedRectangleDrawable(1000f, solidColor, borderColor, borderWidth);
        } else {
            gradientDrawable.setCornerRadius(1000f);
        }
    }

    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    public void setBorderWidth(float width) {
        borderWidth = width;
        if (gradientDrawable == null) {
            gradientDrawable = createRoundedRectangleDrawable(cornerRadius, solidColor, borderColor, borderWidth);
        } else {
            gradientDrawable.setStroke((int) borderWidth, borderColor);
        }
        setBackground(gradientDrawable);
    }

    public void setBorderColor(int color) {
        borderColor = color;
        if (gradientDrawable == null) {
            gradientDrawable = createRoundedRectangleDrawable(cornerRadius, solidColor, borderColor, borderWidth);
        } else {
            gradientDrawable.setStroke((int) borderWidth, borderColor);
        }
        setBackground(gradientDrawable);
    }
}
