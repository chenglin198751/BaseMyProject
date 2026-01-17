package com.wcl.test.view.round;

import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.widget.ImageView.ScaleType;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

public class RoundedDrawable extends Drawable {

    public static final String TAG = "RoundedDrawable";
    public static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    // ===================== Bitmap & Paint =====================

    private final Bitmap mBitmap;
    private final int mBitmapWidth;
    private final int mBitmapHeight;

    private final Paint mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // ===================== Geometry =====================

    private final Geometry mGeometry = new Geometry();

    // ===================== State =====================

    private boolean mOval;
    private float mBorderWidth;
    private ColorStateList mBorderColor =
            ColorStateList.valueOf(DEFAULT_BORDER_COLOR);

    private float mCornerRadius;
    private final boolean[] mCornersRounded = {true, true, true, true};

    private ScaleType mScaleType = ScaleType.FIT_CENTER;
    private Shader.TileMode mTileModeX = Shader.TileMode.CLAMP;
    private Shader.TileMode mTileModeY = Shader.TileMode.CLAMP;

    private boolean mRebuildShader = true;

    // ===================== 构造 =====================

    public RoundedDrawable(Bitmap bitmap) {
        mBitmap = bitmap;
        mBitmapWidth = bitmap.getWidth();
        mBitmapHeight = bitmap.getHeight();

        mBitmapPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setStyle(Paint.Style.STROKE);
        mBorderPaint.setColor(mBorderColor.getDefaultColor());
    }

    // ===================== Factory =====================

    public static RoundedDrawable fromBitmap(Bitmap bitmap) {
        return bitmap != null ? new RoundedDrawable(bitmap) : null;
    }

    public static Drawable fromDrawable(Drawable drawable) {
        if (drawable == null) return null;

        if (drawable instanceof RoundedDrawable) return drawable;

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

        Bitmap bitmap = drawableToBitmap(drawable);
        return bitmap != null ? new RoundedDrawable(bitmap) : drawable;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            int w = Math.max(drawable.getIntrinsicWidth(), 2);
            int h = Math.max(drawable.getIntrinsicHeight(), 2);
            Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            drawable.draw(canvas);
            return bitmap;
        } catch (Throwable t) {
            Log.w(TAG, "drawableToBitmap failed", t);
            return null;
        }
    }

    // ===================== Drawable =====================

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mGeometry.update(
                bounds,
                mBitmapWidth,
                mBitmapHeight,
                mScaleType,
                mBorderWidth
        );
        mRebuildShader = true;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        ensureShader();

        if (mOval) {
            drawOval(canvas);
        } else {
            drawRoundedRect(canvas);
        }
    }

    private void ensureShader() {
        if (!mRebuildShader) return;

        BitmapShader shader = new BitmapShader(mBitmap, mTileModeX, mTileModeY);
        if (mTileModeX == Shader.TileMode.CLAMP &&
                mTileModeY == Shader.TileMode.CLAMP) {
            shader.setLocalMatrix(mGeometry.shaderMatrix);
        }
        mBitmapPaint.setShader(shader);

        mBorderPaint.setStrokeWidth(mBorderWidth);
        mBorderPaint.setColor(
                mBorderColor.getColorForState(getState(), DEFAULT_BORDER_COLOR)
        );

        mRebuildShader = false;
    }

    private void drawOval(Canvas canvas) {
        canvas.drawOval(mGeometry.drawableRect, mBitmapPaint);
        if (mBorderWidth > 0) {
            canvas.drawOval(mGeometry.borderRect, mBorderPaint);
        }
    }

    private void drawRoundedRect(Canvas canvas) {
        if (any(mCornersRounded)) {
            float r = mCornerRadius;
            canvas.drawRoundRect(mGeometry.drawableRect, r, r, mBitmapPaint);
            if (mBorderWidth > 0) {
                canvas.drawRoundRect(mGeometry.borderRect, r, r, mBorderPaint);
            }
            redrawSquareCorners(canvas);
        } else {
            canvas.drawRect(mGeometry.drawableRect, mBitmapPaint);
            if (mBorderWidth > 0) {
                canvas.drawRect(mGeometry.borderRect, mBorderPaint);
            }
        }
    }

    private void redrawSquareCorners(Canvas canvas) {
        if (all(mCornersRounded) || mCornerRadius == 0) return;

        RectF r = mGeometry.drawableRect;
        float radius = mCornerRadius;

        if (!mCornersRounded[RoundedCorner.TOP_LEFT]) {
            canvas.drawRect(r.left, r.top, r.left + radius, r.top + radius, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.TOP_RIGHT]) {
            canvas.drawRect(r.right - radius, r.top, r.right, r.top + radius, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.BOTTOM_RIGHT]) {
            canvas.drawRect(r.right - radius, r.bottom - radius, r.right, r.bottom, mBitmapPaint);
        }
        if (!mCornersRounded[RoundedCorner.BOTTOM_LEFT]) {
            canvas.drawRect(r.left, r.bottom - radius, r.left + radius, r.bottom, mBitmapPaint);
        }
    }

    // ===================== Alpha / Color =====================

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
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    // ===================== public API（保持不变） =====================

    public RoundedDrawable setCornerRadius(float radius) {
        return setCornerRadius(radius, radius, radius, radius);
    }

    public RoundedDrawable setCornerRadius(
            float topLeft, float topRight,
            float bottomRight, float bottomLeft
    ) {
        Set<Float> set = new HashSet<>();
        if (topLeft > 0) set.add(topLeft);
        if (topRight > 0) set.add(topRight);
        if (bottomRight > 0) set.add(bottomRight);
        if (bottomLeft > 0) set.add(bottomLeft);

        if (set.size() > 1) {
            throw new IllegalArgumentException("Multiple nonzero radii not supported");
        }

        mCornerRadius = set.isEmpty() ? 0f : set.iterator().next();
        mCornersRounded[RoundedCorner.TOP_LEFT] = topLeft > 0;
        mCornersRounded[RoundedCorner.TOP_RIGHT] = topRight > 0;
        mCornersRounded[RoundedCorner.BOTTOM_RIGHT] = bottomRight > 0;
        mCornersRounded[RoundedCorner.BOTTOM_LEFT] = bottomLeft > 0;

        invalidateSelf();
        return this;
    }

    public RoundedDrawable setBorderWidth(float width) {
        mBorderWidth = width;
        mRebuildShader = true;
        invalidateSelf();
        return this;
    }

    public RoundedDrawable setBorderColor(@ColorInt int color) {
        return setBorderColor(ColorStateList.valueOf(color));
    }

    public RoundedDrawable setBorderColor(ColorStateList colors) {
        mBorderColor = colors != null ? colors : ColorStateList.valueOf(0);
        invalidateSelf();
        return this;
    }

    public RoundedDrawable setOval(boolean oval) {
        mOval = oval;
        invalidateSelf();
        return this;
    }

    public RoundedDrawable setScaleType(ScaleType scaleType) {
        if (scaleType != null && mScaleType != scaleType) {
            mScaleType = scaleType;
            mRebuildShader = true;
            invalidateSelf();
        }
        return this;
    }

    public RoundedDrawable setTileModeX(Shader.TileMode mode) {
        if (mTileModeX != mode) {
            mTileModeX = mode;
            mRebuildShader = true;
            invalidateSelf();
        }
        return this;
    }

    public RoundedDrawable setTileModeY(Shader.TileMode mode) {
        if (mTileModeY != mode) {
            mTileModeY = mode;
            mRebuildShader = true;
            invalidateSelf();
        }
        return this;
    }

    // ===================== Utils =====================

    private static boolean any(boolean[] b) {
        for (boolean v : b) if (v) return true;
        return false;
    }

    private static boolean all(boolean[] b) {
        for (boolean v : b) if (!v) return false;
        return true;
    }

    public Bitmap toBitmap() {
        return drawableToBitmap(this);
    }

    // ===================== Geometry 内部类 =====================

    private static final class Geometry {
        final RectF drawableRect = new RectF();
        final RectF borderRect = new RectF();
        final Matrix shaderMatrix = new Matrix();

        void update(Rect bounds, int bw, int bh, ScaleType st, float border) {
            RectF b = new RectF(bounds);
            borderRect.set(b);
            borderRect.inset(border / 2, border / 2);

            drawableRect.set(borderRect);

            shaderMatrix.reset();
            RectF bitmap = new RectF(0, 0, bw, bh);
            shaderMatrix.setRectToRect(bitmap, borderRect, scaleTypeToScale(st));
        }

        private static Matrix.ScaleToFit scaleTypeToScale(ScaleType st) {
            switch (st) {
                case FIT_START:
                    return Matrix.ScaleToFit.START;
                case FIT_END:
                    return Matrix.ScaleToFit.END;
                case FIT_XY:
                    return Matrix.ScaleToFit.FILL;
                default:
                    return Matrix.ScaleToFit.CENTER;
            }
        }
    }
}
