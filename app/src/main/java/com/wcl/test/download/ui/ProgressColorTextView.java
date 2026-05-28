package com.wcl.test.download.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.widget.AppCompatTextView;

import com.wcl.test.R;

class ProgressColorTextView extends AppCompatTextView {

    private int progress = 0; // 0 ~ 100
    private final int colorLeft = Color.WHITE;
    private final int colorRight = Color.parseColor("#2979FF");

    public ProgressColorTextView(Context context) {
        super(context);
        init();
    }

    public ProgressColorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setBackgroundResource(R.drawable.download_bg_progress_layer);

        // 设置胶囊形状的左右圆角
        setClipToOutline(true);
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                int width = view.getWidth();
                int height = view.getHeight();
                float radius = height / 2f;
                outline.setRoundRect(0, 0, width, height, radius);
            }
        });
    }

    /**
     * 设置进度（0~100）
     */
    void setProgress(double p) {
        this.progress = (int) Math.round(Math.max(0, Math.min(p, 100)));

        // 1.设置文字变色进度
        updateLinearGradient();
        invalidate();

        // 2.设置背景进度条
        updateBackgroundProgress();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateLinearGradient();
    }

    /**
     * 更新文字变色
     */
    private void updateLinearGradient() {
        int width = getWidth();
        if (width <= 0) return;

        setTextColor(colorLeft);
        float p = progress / 100f;
        LinearGradient shader = new LinearGradient(
                0, 0,
                width, 0,
                new int[]{colorLeft, colorLeft, colorRight, colorRight},
                new float[]{0f, p, p, 1f},
                Shader.TileMode.CLAMP
        );
        getPaint().setShader(shader);
    }

    /**
     * 更新背景进度条
     */
    private void updateBackgroundProgress() {
        Drawable bg = getBackground();
        if (bg instanceof LayerDrawable layer) {
            Drawable progressDrawable = layer.getDrawable(1);
            progressDrawable.setLevel(progress * 100);
        }
    }
}
