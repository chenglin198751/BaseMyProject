package com.wcl.test.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.wcl.test.R;

public class SimpleViewPagerIndicator extends View {

    private String[] titles = new String[0];

    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Drawable indicatorDrawable;

    private float textSize;
    private float defaultIndicatorHeight;

    private int normalColor = 0xFF999999;
    private int selectedColor = 0xFF222222;

    private float[] textWidths;
    private float[] textCenters;

    private int currentPosition = 0;
    private float positionOffset = 0f;

    private ViewPager viewPager;

    // 用户可设置的 indicator 尺寸
    private Float fixedIndicatorWidth = null;
    private Float fixedIndicatorHeight = null;

    public SimpleViewPagerIndicator(Context context) {
        super(context);
        init();
    }

    public SimpleViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textSize = dp(14);
        defaultIndicatorHeight = dp(3);

        textPaint.setTextSize(textSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        indicatorDrawable = ContextCompat.getDrawable(getContext(), R.drawable.common_tab_indicator);
        setClickable(true);
    }

    // ===== 对外 API =====

    public void setTitles(String... titles) {
        this.titles = titles != null ? titles : new String[0];
        requestLayout();
        invalidate();
    }

    public void attachViewPager(ViewPager vp) {
        this.viewPager = vp;
        vp.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float offset, int offsetPixels) {
                currentPosition = position;
                positionOffset = offset;
                invalidate();
            }
        });
    }

    public void setIndicatorWidth(float dp) {
        fixedIndicatorWidth = dp(dp);
        invalidate();
    }

    public void setIndicatorHeight(float dp) {
        fixedIndicatorHeight = dp(dp);
        requestLayout();
        invalidate();
    }

    // ===== 测量 =====

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int w = MeasureSpec.getSize(widthMeasureSpec);

        float hIndicator = fixedIndicatorHeight != null ? fixedIndicatorHeight : defaultIndicatorHeight;
        int h = (int) (dp(40) + hIndicator);

        setMeasuredDimension(w, h);
        calculateTextLayout(w);
    }

    private void calculateTextLayout(int width) {
        int count = titles.length;
        if (count == 0) return;

        textWidths = new float[count];
        textCenters = new float[count];

        float totalTextWidth = 0;
        for (int i = 0; i < count; i++) {
            textWidths[i] = textPaint.measureText(titles[i]);
            totalTextWidth += textWidths[i];
        }

        float space = (width - totalTextWidth) / (count + 1);
        float x = space;

        for (int i = 0; i < count; i++) {
            x += textWidths[i] / 2;
            textCenters[i] = x;
            x += textWidths[i] / 2 + space;
        }
    }

    // ===== 绘制 =====

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (titles.length == 0) return;

        float textY = dp(26);

        for (int i = 0; i < titles.length; i++) {
            textPaint.setColor(getTextColor(i));
            canvas.drawText(titles[i], textCenters[i], textY, textPaint);
        }

        drawIndicator(canvas);
    }

    private int getTextColor(int index) {
        if (index == currentPosition) {
            return blendColor(selectedColor, normalColor, positionOffset);
        } else if (index == currentPosition + 1) {
            return blendColor(normalColor, selectedColor, positionOffset);
        }
        return normalColor;
    }

    private void drawIndicator(Canvas canvas) {
        int next = Math.min(currentPosition + 1, titles.length - 1);

        float startCenter = textCenters[currentPosition];
        float endCenter = textCenters[next];

        float startWidth = textWidths[currentPosition];
        float endWidth = textWidths[next];

        float center = lerp(startCenter, endCenter, positionOffset);

        float width = fixedIndicatorWidth != null
                ? fixedIndicatorWidth
                : lerp(startWidth, endWidth, positionOffset);

        float height = fixedIndicatorHeight != null
                ? fixedIndicatorHeight
                : defaultIndicatorHeight;

        float left = center - width / 2;
        float right = center + width / 2;
        float bottom = getHeight();
        float top = bottom - height;

        indicatorDrawable.setBounds((int) left, (int) top, (int) right, (int) bottom);
        indicatorDrawable.draw(canvas);
    }

    // ===== 点击支持 =====

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP && viewPager != null) {
            float x = event.getX();
            for (int i = 0; i < textCenters.length; i++) {
                float left = i == 0 ? 0 : (textCenters[i - 1] + textCenters[i]) / 2;
                float right = i == textCenters.length - 1 ? getWidth() : (textCenters[i] + textCenters[i + 1]) / 2;
                if (x >= left && x <= right) {
                    viewPager.setCurrentItem(i, true);
                    break;
                }
            }
        }
        return true;
    }

    // ===== 工具 =====

    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private int blendColor(int from, int to, float t) {
        int fr = (from >> 16) & 0xff;
        int fg = (from >> 8) & 0xff;
        int fb = from & 0xff;

        int tr = (to >> 16) & 0xff;
        int tg = (to >> 8) & 0xff;
        int tb = to & 0xff;

        int r = (int) (fr + (tr - fr) * t);
        int g = (int) (fg + (tg - fg) * t);
        int b = (int) (fb + (tb - fb) * t);

        return 0xff000000 | (r << 16) | (g << 8) | b;
    }

    private float dp(float v) {
        return v * getResources().getDisplayMetrics().density;
    }
}
