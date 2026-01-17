package com.wcl.test.view.round;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

public class RoundedImageView extends AppCompatImageView {

    private float cornerRadius = 0f;
    private boolean isOval = false;
    private float aspectRatio = 0f;
    private float borderWidth = 0f;
    private int borderColor = Color.BLACK;
    private Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
            borderColor = a.getColor(R.styleable.RoundedImageView_riv_border_color, Color.BLACK);
            a.recycle();
        }

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setColor(borderColor);
        borderPaint.setStrokeWidth(borderWidth);

        setupOutlineProvider();
        setClipToOutline(true);
    }

    private void setupOutlineProvider() {
        setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                float half = borderWidth / 2f;
                if (isOval) {
                    outline.setOval(
                            (int) half,
                            (int) half,
                            (int) (getWidth() - half),
                            (int) (getHeight() - half)
                    );
                } else {
                    outline.setRoundRect(
                            (int) half,
                            (int) half,
                            (int) (getWidth() - half),
                            (int) (getHeight() - half),
                            cornerRadius
                    );
                }
            }
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (aspectRatio > 0) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = (int) (width / aspectRatio);
            setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (borderWidth > 0) {
            float half = borderWidth / 2f;
            if (isOval) {
                canvas.drawOval(
                        half,
                        half,
                        getWidth() - half,
                        getHeight() - half,
                        borderPaint
                );
            } else {
                canvas.drawRoundRect(
                        half,
                        half,
                        getWidth() - half,
                        getHeight() - half,
                        cornerRadius,
                        cornerRadius,
                        borderPaint
                );
            }
        }
    }

    // ---------------- Public API ----------------

    public void setCornerRadius(float radius) {
        cornerRadius = radius;
        setupOutlineProvider();
        invalidate();
    }

    public void setOval(boolean oval) {
        isOval = oval;
        setupOutlineProvider();
        invalidate();
    }

    public void setAspectRatio(float ratio) {
        aspectRatio = ratio;
        requestLayout();
    }

    public void setBorderWidth(float width) {
        borderWidth = width;
        borderPaint.setStrokeWidth(width);
        setupOutlineProvider();
        invalidate();
    }

    public void setBorderColor(int color) {
        borderColor = color;
        borderPaint.setColor(color);
        invalidate();
    }
}
