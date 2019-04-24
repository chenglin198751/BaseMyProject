package cheerly.mybaseproject.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;


public class HollowTextView extends AppCompatTextView {
    private Paint mTextPaint, mBackgroundPaint;
    private Bitmap mBackgroundBitmap, mTextBitmap;
    private Canvas mBackgroundCanvas, mTextCanvas;
    private RectF mBackgroundRect;
    private int mBackgroundColor;
    private int mCornerRadius;

    public HollowTextView(Context context) {
        this(context, null);
    }

    public HollowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public HollowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr);
        initPaint();
    }


    private void initAttrs(AttributeSet attrs, int defStyleAttr) {
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HollowTextView, defStyleAttr, 0);
        mBackgroundColor = a.getResourceId(R.styleable.HollowTextView_background_color, Color.TRANSPARENT);
        mCornerRadius = BaseUtils.dip2px(a.getInt(R.styleable.HollowTextView_corner_radius, 0));
        a.recycle();
    }


    /**
     * 设置文本框背景色和文本框圆角角度
     *
     * @param color  文本框背景色
     * @param radius 文本框圆角角度
     */
    public void setBgColorAndRadius(@ColorInt int color, @IntRange(from = 0) int radius) {
        mBackgroundColor = color;
        mBackgroundPaint.setColor(mBackgroundColor);
        mCornerRadius = radius;
        invalidate();
    }


    /***
     * 初始化画笔属性
     */
    private void initPaint() {
        //画文字的paint
        mTextPaint = new Paint();
        //这是镂空的关键
        mTextPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mTextPaint.setAntiAlias(true);
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(mBackgroundColor);
        mBackgroundPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        mBackgroundCanvas = new Canvas(mBackgroundBitmap);
        mTextBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
        mTextCanvas = new Canvas(mTextBitmap);
        mBackgroundRect = new RectF(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //这里给super传入的是mTextCanvas，把一些基本属性都支持进去
        super.onDraw(mTextCanvas);
        drawBackground(mBackgroundCanvas);
        int sc;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sc = canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), null);
        } else {
            sc = canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), null, Canvas.ALL_SAVE_FLAG);
        }
        canvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
        canvas.drawBitmap(mTextBitmap, 0, 0, mTextPaint);
        canvas.restoreToCount(sc);
    }

    private void drawBackground(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        if (mCornerRadius > 0) {
            canvas.drawRoundRect(mBackgroundRect, mCornerRadius, mCornerRadius, mBackgroundPaint);
        } else {
            canvas.drawColor(mBackgroundColor);
        }
    }
}