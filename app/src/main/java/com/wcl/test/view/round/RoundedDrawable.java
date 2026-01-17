package com.wcl.test.view.round;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
public class RoundedDrawable extends Drawable {

    public static final String TAG = "RoundedDrawable";
    public static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    /** ========== 基础数据 ========== */

    private final Bitmap mBitmap;
    private final int mBitmapWidth;
    private final int mBitmapHeight;

    private final RectF mBitmapRect = new RectF();
    private final RectF mBoundsRect = new RectF();
    private final RectF mDrawableRect = new RectF();
    private final RectF mBorderRect = new RectF();

    private final Matrix mShaderMatrix = new Matrix();

    /** ========== Paint ========== */

    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /** ========== 圆角 / 边框 ========== */

    private float mCornerRadius = 0f;
    // 顺序：[TL, TR, BR, BL]
    private final boolean[] mCornersRounded = {true, true, true, true};

    private boolean mOval = false;

    private float mBorderWidth = 0f;
    private ColorStateList mBorderColor =
            ColorStateList.valueOf(DEFAULT_BORDER_COLOR);

    /** ========== Shader / Scale ========== */

    private Shader.TileMode mTileModeX = Shader.TileMode.CLAMP;
    private Shader.TileMode mTileModeY = Shader.TileMode.CLAMP;

    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private boolean mShaderDirty = true;

    /** ========== 构造 ========== */

    public RoundedDrawable(Bitmap bitmap) {
        mBitmap = bitmap;
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();

        mBitmapRect.set(0, 0, mBitmapWidth, mBitmapHeight);

        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor.getDefaultColor());
        mBorderPaint.setStrokeWidth(mBorderWidth);
    }

    /** ========== Factory ========== */

    public static RoundedDrawable fromBitmap(Bitmap bitmap) {
        return bitmap != null ? new RoundedDrawable(bitmap) : null;
    }

    public static Drawable fromDrawable(Drawable drawable) {
        if (drawable == null) return null;

        if (drawable instanceof RoundedDrawable) {
            return drawable;
        }

        if (drawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) drawable;
            for (int i = 0; i < ld.getNumberOfLayers(); i++) {
                ld.setDrawableByLayerId(
                        ld.getId(i),
                        fromDrawable(ld.getDrawable(i))
                );
            }
            return ld;
        }

        Bitmap bm = drawableToBitmap(drawable);
        return bm != null ? new RoundedDrawable(bm) : drawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            int w = Math.max(drawable.getIntrinsicWidth(), 2);
            int h = Math.max(drawable.getIntrinsicHeight(), 2);
            Bitmap bitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            Log.w(TAG, "Failed to create bitmap", e);
            return null;
        }
    }

    /** ========== Drawable 生命周期 ========== */

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mBoundsRect.set(bounds);
        rebuildShaderMatrix();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        ensureShader();

        if (mOval) {
            drawOval(canvas);
        } else if (hasAnyRoundCorner()) {
            drawRoundRect(canvas);
        } else {
            drawRect(canvas);
        }
    }

    private void ensureShader() {
        if (!mShaderDirty) return;

        BitmapShader shader = new BitmapShader(
                mBitmap, mTileModeX, mTileModeY);

        if (mTileModeX == Shader.TileMode.CLAMP &&
                mTileModeY == Shader.TileMode.CLAMP) {
            shader.setLocalMatrix(mShaderMatrix);
        }

        mBitmapPaint.setShader(shader);
        mShaderDirty = false;
    }

    /** ========== 绘制实现 ========== */

    private void drawOval(Canvas canvas) {
        canvas.drawOval(mDrawableRect, mBitmapPaint);
        if (mBorderWidth > 0) {
            canvas.drawOval(mBorderRect, mBorderPaint);
        }
    }

    private void drawRect(Canvas canvas) {
        canvas.drawRect(mDrawableRect, mBitmapPaint);
        if (mBorderWidth > 0) {
            canvas.drawRect(mBorderRect, mBorderPaint);
        }
    }

    private void drawRoundRect(Canvas canvas) {
        float r = mCornerRadius;
        canvas.drawRoundRect(mDrawableRect, r, r, mBitmapPaint);

        if (mBorderWidth > 0) {
            canvas.drawRoundRect(mBorderRect, r, r, mBorderPaint);
        }

        redrawSquareCorners(canvas);
    }

    /** ========== Square Corner 修正 ========== */

    private final RectF mTmpRect = new RectF();

    private void redrawSquareCorners(Canvas canvas) {
        if (isAllCornersRounded() || mCornerRadius == 0) return;

        float l = mDrawableRect.left;
        float t = mDrawableRect.top;
        float r = mDrawableRect.right;
        float b = mDrawableRect.bottom;
        float cr = mCornerRadius;

        if (!mCornersRounded[RoundedCorner.TOP_LEFT]) {
            mTmpRect.set(l, t, l + cr, t + cr);
            canvas.drawRect(mTmpRect, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.TOP_RIGHT]) {
            mTmpRect.set(r - cr, t, r, t + cr);
            canvas.drawRect(mTmpRect, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.BOTTOM_RIGHT]) {
            mTmpRect.set(r - cr, b - cr, r, b);
            canvas.drawRect(mTmpRect, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.BOTTOM_LEFT]) {
            mTmpRect.set(l, b - cr, l + cr, b);
            canvas.drawRect(mTmpRect, mBitmapPaint);
        }
    }

    /** ========== Matrix / ScaleType ========== */

    private void rebuildShaderMatrix() {
        mShaderMatrix.reset();

        mBorderRect.set(mBoundsRect);
        mBorderRect.inset(mBorderWidth / 2f, mBorderWidth / 2f);

        switch (mScaleType) {
            case CENTER:
                center();
                break;
            case CENTER_CROP:
                centerCrop();
                break;
            case CENTER_INSIDE:
                centerInside();
                break;
            case FIT_XY:
                fitXY();
                break;
            default:
                fitCenter();
                break;
        }

        mDrawableRect.set(mBorderRect);
        mShaderDirty = true;
        invalidateSelf();
    }

    private void center() {
        float dx = (mBorderRect.width() - mBitmapWidth) * 0.5f;
        float dy = (mBorderRect.height() - mBitmapHeight) * 0.5f;
        mShaderMatrix.setTranslate(dx, dy);
    }

    private void centerCrop() {
        float scale;
        float dx = 0, dy = 0;

        if (mBitmapWidth * mBorderRect.height() >
                mBorderRect.width() * mBitmapHeight) {
            scale = mBorderRect.height() / mBitmapHeight;
            dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f;
        } else {
            scale = mBorderRect.width() / mBitmapWidth;
            dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f;
        }

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate(dx, dy);
    }

    private void centerInside() {
        float scale = Math.min(
                mBorderRect.width() / mBitmapWidth,
                mBorderRect.height() / mBitmapHeight
        );
        scale = Math.min(scale, 1f);

        float dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f;
        float dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f;

        mShaderMatrix.setScale(scale, scale);
        mShaderMatrix.postTranslate(dx, dy);
    }

    private void fitCenter() {
        mShaderMatrix.setRectToRect(
                mBitmapRect, mBorderRect, Matrix.ScaleToFit.CENTER);
    }

    private void fitXY() {
        mShaderMatrix.setRectToRect(
                mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL);
    }

    /** ========== 状态 & Getter ========== */

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public boolean isStateful() {
        return mBorderColor.isStateful();
    }

    @Override
    protected boolean onStateChange(int[] state) {
        int color = mBorderColor.getColorForState(
                state, mBorderPaint.getColor());
        if (mBorderPaint.getColor() != color) {
            mBorderPaint.setColor(color);
            invalidateSelf();
            return true;
        }
        return false;
    }

    @Override
    public void setAlpha(int alpha) {
        mBitmapPaint.setAlpha(alpha);
        invalidateSelf();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mBitmapPaint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    /** ========== Public API（保持不变） ========== */

    public float getCornerRadius() {
        return mCornerRadius;
    }

    public float getCornerRadius(@RoundedCorner int corner) {
        return mCornersRounded[corner] ? mCornerRadius : 0f;
    }

    public RoundedDrawable setCornerRadius(float radius) {
        setCornerRadius(radius, radius, radius, radius);
        return this;
    }

    public RoundedDrawable setCornerRadius(
            float tl, float tr, float br, float bl) {

        Set<Float> set = new HashSet<>(4);
        if (tl > 0) set.add(tl);
        if (tr > 0) set.add(tr);
        if (br > 0) set.add(br);
        if (bl > 0) set.add(bl);

        if (set.size() > 1) {
            throw new IllegalArgumentException(
                    "Multiple nonzero corner radii not supported");
        }

        mCornerRadius = set.isEmpty() ? 0f : set.iterator().next();

        mCornersRounded[RoundedCorner.TOP_LEFT] = tl > 0;
        mCornersRounded[RoundedCorner.TOP_RIGHT] = tr > 0;
        mCornersRounded[RoundedCorner.BOTTOM_RIGHT] = br > 0;
        mCornersRounded[RoundedCorner.BOTTOM_LEFT] = bl > 0;

        invalidateSelf();
        return this;
    }

    public float getBorderWidth() {
        return mBorderWidth;
    }

    public RoundedDrawable setBorderWidth(float width) {
        mBorderWidth = width;
        mBorderPaint.setStrokeWidth(width);
        rebuildShaderMatrix();
        return this;
    }

    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public RoundedDrawable setBorderColor(@ColorInt int color) {
        return setBorderColor(ColorStateList.valueOf(color));
    }

    public RoundedDrawable setBorderColor(ColorStateList colors) {
        mBorderColor = colors != null ? colors : ColorStateList.valueOf(0);
        mBorderPaint.setColor(
                mBorderColor.getColorForState(getState(), DEFAULT_BORDER_COLOR));
        invalidateSelf();
        return this;
    }

    public boolean isOval() {
        return mOval;
    }

    public RoundedDrawable setOval(boolean oval) {
        mOval = oval;
        invalidateSelf();
        return this;
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    public RoundedDrawable setScaleType(ScaleType scaleType) {
        if (scaleType == null) scaleType = ScaleType.FIT_CENTER;
        if (mScaleType != scaleType) {
            mScaleType = scaleType;
            rebuildShaderMatrix();
        }
        return this;
    }

    public Shader.TileMode getTileModeX() {
        return mTileModeX;
    }

    public RoundedDrawable setTileModeX(Shader.TileMode mode) {
        if (mTileModeX != mode) {
            mTileModeX = mode;
            mShaderDirty = true;
            invalidateSelf();
        }
        return this;
    }

    public Shader.TileMode getTileModeY() {
        return mTileModeY;
    }

    public RoundedDrawable setTileModeY(Shader.TileMode mode) {
        if (mTileModeY != mode) {
            mTileModeY = mode;
            mShaderDirty = true;
            invalidateSelf();
        }
        return this;
    }

    public Bitmap getSourceBitmap() {
        return mBitmap;
    }

    public Bitmap toBitmap() {
        return drawableToBitmap(this);
    }

    /** ========== Corner 工具 ========== */

    private boolean hasAnyRoundCorner() {
        for (boolean b : mCornersRounded) {
            if (b) return true;
        }
        return false;
    }

    private boolean isAllCornersRounded() {
        for (boolean b : mCornersRounded) {
            if (!b) return false;
        }
        return true;
    }
}
