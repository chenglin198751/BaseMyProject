package com.wcl.test.download.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

import com.wcl.test.R;

class ProgressColorTextView extends AppCompatTextView {

    private int progress = 0; // 0 ~ 100
    private final int colorLeft = Color.WHITE;
    private final int colorRight = Color.parseColor("#2979FF");

    public ProgressColorTextView(Context context) {
        super(context);
        init(context, null);
    }

    public ProgressColorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProgressColorTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setBackgroundResource(R.drawable.download_bg_progress_layer);
    }

    /**
     * 设置进度（0~100）
     */
    public void setProgress(int progress) {
        int newProgress = Math.max(0, Math.min(progress, 100));
        if (this.progress != newProgress) {
            // 1.设置文字变色进度
            this.progress = newProgress;
            updateShader();
            invalidate();

            // 2.设置背景进度条
            Drawable bg = getBackground();
            if (bg instanceof LayerDrawable layer) {
                Drawable progressDrawable = layer.getDrawable(1);
                progressDrawable.setLevel(progress * 100);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        updateShader();
    }

    private void updateShader() {
        int width = getWidth();
        if (width <= 0) return;

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
}
