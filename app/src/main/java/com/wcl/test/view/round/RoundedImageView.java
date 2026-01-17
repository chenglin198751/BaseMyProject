package com.wcl.test.view.round;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

public class RoundedImageView extends AppCompatImageView {

    public static final String TAG = "RoundedImageView";

    public static final float DEFAULT_RADIUS = 0f;
    public static final float DEFAULT_BORDER_WIDTH = 0f;
    public static final Shader.TileMode DEFAULT_TILE_MODE = Shader.TileMode.CLAMP;

    private static final int TILE_MODE_UNDEFINED = -2;
    private static final int TILE_MODE_CLAMP = 0;
    private static final int TILE_MODE_REPEAT = 1;
    private static final int TILE_MODE_MIRROR = 2;

    private static final ScaleType[] SCALE_TYPES = {
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
    };

    /** ========== 状态字段 ========== */

    private final float[] mCornerRadii = new float[]{
            DEFAULT_RADIUS, DEFAULT_RADIUS,
            DEFAULT_RADIUS, DEFAULT_RADIUS
    };

    private Drawable mDrawable;
    private Drawable mBackgroundDrawable;

    private int mResourceId;
    private int mBackgroundResId;

    private float mBorderWidth = DEFAULT_BORDER_WIDTH;
    private ColorStateList mBorderColor =
            ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);

    private boolean mIsOval = false;
    private boolean mMutateBackground = false;

    private Shader.TileMode mTileModeX = DEFAULT_TILE_MODE;
    private Shader.TileMode mTileModeY = DEFAULT_TILE_MODE;

    private ScaleType mScaleType = ScaleType.CENTER_CROP;

    private ColorFilter mColorFilter;
    private boolean mHasColorFilter;
    private boolean mColorMod;

    /** ========== 构造 ========== */

    public RoundedImageView(Context context) {
        super(context);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    /** ========== 初始化 ========== */

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RoundedImageView, defStyleAttr, 0);

        // scaleType
        int scaleIndex = a.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
        setScaleType(scaleIndex >= 0 ? SCALE_TYPES[scaleIndex] : ScaleType.CENTER_CROP);

        // corner radius
        float globalRadius =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1);

        mCornerRadii[RoundedCorner.TOP_LEFT] =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_left, -1);
        mCornerRadii[RoundedCorner.TOP_RIGHT] =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_right, -1);
        mCornerRadii[RoundedCorner.BOTTOM_RIGHT] =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_bottom_right, -1);
        mCornerRadii[RoundedCorner.BOTTOM_LEFT] =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_bottom_left, -1);

        boolean anyCustom = false;
        for (int i = 0; i < mCornerRadii.length; i++) {
            if (mCornerRadii[i] >= 0) {
                anyCustom = true;
            } else {
                mCornerRadii[i] = 0f;
            }
        }

        if (!anyCustom) {
            float r = globalRadius >= 0 ? globalRadius : DEFAULT_RADIUS;
            for (int i = 0; i < mCornerRadii.length; i++) {
                mCornerRadii[i] = r;
            }
        }

        // border
        mBorderWidth = a.getDimensionPixelSize(
                R.styleable.RoundedImageView_riv_border_width,
                (int) DEFAULT_BORDER_WIDTH);

        ColorStateList border =
                a.getColorStateList(R.styleable.RoundedImageView_riv_border_color);
        if (border != null) {
            mBorderColor = border;
        }

        mIsOval = a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);
        mMutateBackground =
                a.getBoolean(R.styleable.RoundedImageView_riv_mutate_background, false);

        int tileMode = a.getInt(
                R.styleable.RoundedImageView_riv_tile_mode, TILE_MODE_UNDEFINED);
        if (tileMode != TILE_MODE_UNDEFINED) {
            Shader.TileMode tm = parseTileMode(tileMode);
            mTileModeX = tm;
            mTileModeY = tm;
        }

        int tileX = a.getInt(
                R.styleable.RoundedImageView_riv_tile_mode_x, TILE_MODE_UNDEFINED);
        if (tileX != TILE_MODE_UNDEFINED) {
            mTileModeX = parseTileMode(tileX);
        }

        int tileY = a.getInt(
                R.styleable.RoundedImageView_riv_tile_mode_y, TILE_MODE_UNDEFINED);
        if (tileY != TILE_MODE_UNDEFINED) {
            mTileModeY = parseTileMode(tileY);
        }

        a.recycle();

        refreshDrawable();
        refreshBackground(true);
    }

    /** ========== ScaleType ========== */

    @Override
    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null || mScaleType == scaleType) return;

        mScaleType = scaleType;

        switch (scaleType) {
            case CENTER:
            case CENTER_CROP:
            case CENTER_INSIDE:
            case FIT_CENTER:
            case FIT_START:
            case FIT_END:
            case FIT_XY:
                super.setScaleType(ScaleType.FIT_XY);
                break;
            default:
                super.setScaleType(scaleType);
        }

        refreshDrawable();
        refreshBackground(false);
    }

    /** ========== Image 设置 ========== */

    @Override
    public void setImageDrawable(Drawable drawable) {
        mResourceId = 0;
        mDrawable = RoundedDrawable.fromDrawable(drawable);
        refreshDrawable();
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        mResourceId = 0;
        mDrawable = RoundedDrawable.fromBitmap(bm);
        refreshDrawable();
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        if (mResourceId == resId) return;
        mResourceId = resId;
        mDrawable = resolveDrawable(resId);
        refreshDrawable();
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    /** ========== Background ========== */

    @Override
    public void setBackground(Drawable background) {
        setBackgroundDrawable(background);
    }

    @Override
    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
        refreshBackground(true);
        super.setBackgroundDrawable(mBackgroundDrawable);
    }

    @Override
    public void setBackgroundResource(@DrawableRes int resId) {
        if (mBackgroundResId == resId) return;
        mBackgroundResId = resId;
        mBackgroundDrawable = resolveDrawable(resId);
        setBackgroundDrawable(mBackgroundDrawable);
    }

    @Override
    public void setBackgroundColor(int color) {
        setBackgroundDrawable(new ColorDrawable(color));
    }

    /** ========== 刷新逻辑（核心） ========== */

    private void refreshDrawable() {
        applyAttrs(mDrawable, mScaleType);
        applyColorMod();
        invalidate();
    }

    private void refreshBackground(boolean convert) {
        if (!mMutateBackground) return;

        if (convert) {
            mBackgroundDrawable =
                    RoundedDrawable.fromDrawable(mBackgroundDrawable);
        }
        applyAttrs(mBackgroundDrawable, ScaleType.FIT_XY);
        invalidate();
    }

    private void applyAttrs(Drawable d, ScaleType scaleType) {
        if (d == null) return;

        if (d instanceof RoundedDrawable) {
            RoundedDrawable rd = (RoundedDrawable) d;
            rd.setScaleType(scaleType)
                    .setBorderWidth(mBorderWidth)
                    .setBorderColor(mBorderColor)
                    .setOval(mIsOval)
                    .setTileModeX(mTileModeX)
                    .setTileModeY(mTileModeY)
                    .setCornerRadius(
                            mCornerRadii[RoundedCorner.TOP_LEFT],
                            mCornerRadii[RoundedCorner.TOP_RIGHT],
                            mCornerRadii[RoundedCorner.BOTTOM_RIGHT],
                            mCornerRadii[RoundedCorner.BOTTOM_LEFT]
                    );
        } else if (d instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) d;
            for (int i = 0; i < ld.getNumberOfLayers(); i++) {
                applyAttrs(ld.getDrawable(i), scaleType);
            }
        }
    }

    /** ========== ColorFilter ========== */

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mColorFilter == cf) return;
        mColorFilter = cf;
        mHasColorFilter = true;
        mColorMod = true;
        applyColorMod();
        invalidate();
    }

    private void applyColorMod() {
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable.mutate();
            if (mHasColorFilter) {
                mDrawable.setColorFilter(mColorFilter);
            }
        }
    }

    /** ========== Utils ========== */

    private Drawable resolveDrawable(int resId) {
        try {
            return RoundedDrawable.fromDrawable(
                    getResources().getDrawable(resId));
        } catch (Exception e) {
            Log.w(TAG, "Unable to find resource: " + resId, e);
            return null;
        }
    }

    private static Shader.TileMode parseTileMode(int mode) {
        switch (mode) {
            case TILE_MODE_CLAMP: return Shader.TileMode.CLAMP;
            case TILE_MODE_REPEAT: return Shader.TileMode.REPEAT;
            case TILE_MODE_MIRROR: return Shader.TileMode.MIRROR;
            default: return null;
        }
    }

    /** ========== 以下 public API 全部保持不变 ========== */

    public float getCornerRadius() { return getMaxCornerRadius(); }

    public float getMaxCornerRadius() {
        float max = 0;
        for (float r : mCornerRadii) max = Math.max(max, r);
        return max;
    }

    public float getCornerRadius(@RoundedCorner int corner) {
        return mCornerRadii[corner];
    }

    public void setCornerRadiusDimen(@DimenRes int resId) {
        float r = getResources().getDimension(resId);
        setCornerRadius(r);
    }

    public void setCornerRadius(@RoundedCorner int corner, float radius) {
        if (mCornerRadii[corner] == radius) return;
        mCornerRadii[corner] = radius;
        refreshDrawable();
        refreshBackground(false);
    }

    public void setCornerRadius(float radius) {
        setCornerRadius(radius, radius, radius, radius);
    }

    public void setCornerRadius(float tl, float tr, float br, float bl) {
        mCornerRadii[RoundedCorner.TOP_LEFT] = tl;
        mCornerRadii[RoundedCorner.TOP_RIGHT] = tr;
        mCornerRadii[RoundedCorner.BOTTOM_RIGHT] = br;
        mCornerRadii[RoundedCorner.BOTTOM_LEFT] = bl;
        refreshDrawable();
        refreshBackground(false);
    }

    public float getBorderWidth() { return mBorderWidth; }

    public void setBorderWidth(float width) {
        if (mBorderWidth == width) return;
        mBorderWidth = width;
        refreshDrawable();
        refreshBackground(false);
    }

    @ColorInt
    public int getBorderColor() {
        return mBorderColor.getDefaultColor();
    }

    public void setBorderColor(@ColorInt int color) {
        setBorderColor(ColorStateList.valueOf(color));
    }

    public void setBorderColor(ColorStateList colors) {
        if (mBorderColor.equals(colors)) return;
        mBorderColor = colors;
        refreshDrawable();
        refreshBackground(false);
    }

    public boolean isOval() { return mIsOval; }

    public void setOval(boolean oval) {
        if (mIsOval == oval) return;
        mIsOval = oval;
        refreshDrawable();
        refreshBackground(false);
    }

    public boolean mutatesBackground() {
        return mMutateBackground;
    }

    public void mutateBackground(boolean mutate) {
        if (mMutateBackground == mutate) return;
        mMutateBackground = mutate;
        refreshBackground(true);
    }

    public Shader.TileMode getTileModeX() { return mTileModeX; }

    public void setTileModeX(Shader.TileMode mode) {
        if (mTileModeX == mode) return;
        mTileModeX = mode;
        refreshDrawable();
        refreshBackground(false);
    }

    public Shader.TileMode getTileModeY() { return mTileModeY; }

    public void setTileModeY(Shader.TileMode mode) {
        if (mTileModeY == mode) return;
        mTileModeY = mode;
        refreshDrawable();
        refreshBackground(false);
    }
}
