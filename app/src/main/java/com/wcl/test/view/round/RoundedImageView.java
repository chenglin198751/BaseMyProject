package com.wcl.test.view.round;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.ColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.DrawableRes;
import androidx.appcompat.widget.AppCompatImageView;

import com.wcl.test.R;

public class RoundedImageView extends AppCompatImageView {

    public static final String TAG = "RoundedImageView";

    private static final Shader.TileMode DEFAULT_TILE_MODE = Shader.TileMode.CLAMP;

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

    // ===================== 内部配置 =====================

    private static final class RoundedConfig {
        final float[] cornerRadii = new float[4];
        float borderWidth = 0f;
        ColorStateList borderColor =
                ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
        boolean isOval = false;
        Shader.TileMode tileModeX = DEFAULT_TILE_MODE;
        Shader.TileMode tileModeY = DEFAULT_TILE_MODE;
        ScaleType scaleType = ScaleType.CENTER_CROP;
    }

    private final RoundedConfig mConfig = new RoundedConfig();

    // ===================== Drawable 状态 =====================

    private Drawable mDrawable;
    private Drawable mBackgroundDrawable;
    private int mImageResId;
    private int mBackgroundResId;

    private boolean mMutateBackground = false;

    private ColorFilter mColorFilter;
    private boolean mHasColorFilter;

    // ===================== 构造函数 =====================

    public RoundedImageView(Context context) {
        super(context);
        initDefaults();
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initDefaults();
        parseAttrs(context, attrs, defStyleAttr);
    }

    private void initDefaults() {
        for (int i = 0; i < 4; i++) {
            mConfig.cornerRadii[i] = 0f;
        }
    }

    // ===================== attrs 解析 =====================

    private void parseAttrs(Context context, AttributeSet attrs, int defStyle) {
        TypedArray a =
                context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0);

        int scaleTypeIndex =
                a.getInt(R.styleable.RoundedImageView_android_scaleType, -1);
        if (scaleTypeIndex >= 0) {
            setScaleType(SCALE_TYPES[scaleTypeIndex]);
        }

        float allRadius =
                a.getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1);

        readCorner(a, RoundedCorner.TOP_LEFT,
                R.styleable.RoundedImageView_riv_corner_radius_top_left);
        readCorner(a, RoundedCorner.TOP_RIGHT,
                R.styleable.RoundedImageView_riv_corner_radius_top_right);
        readCorner(a, RoundedCorner.BOTTOM_RIGHT,
                R.styleable.RoundedImageView_riv_corner_radius_bottom_right);
        readCorner(a, RoundedCorner.BOTTOM_LEFT,
                R.styleable.RoundedImageView_riv_corner_radius_bottom_left);

        boolean hasAny = false;
        for (float r : mConfig.cornerRadii) {
            if (r > 0) {
                hasAny = true;
                break;
            }
        }

        if (!hasAny && allRadius >= 0) {
            for (int i = 0; i < 4; i++) {
                mConfig.cornerRadii[i] = allRadius;
            }
        }

        mConfig.borderWidth =
                a.getDimensionPixelSize(
                        R.styleable.RoundedImageView_riv_border_width, 0);

        ColorStateList border =
                a.getColorStateList(R.styleable.RoundedImageView_riv_border_color);
        if (border != null) {
            mConfig.borderColor = border;
        }

        mConfig.isOval =
                a.getBoolean(R.styleable.RoundedImageView_riv_oval, false);

        mMutateBackground =
                a.getBoolean(R.styleable.RoundedImageView_riv_mutate_background, false);

        int tileMode =
                a.getInt(R.styleable.RoundedImageView_riv_tile_mode, -1);
        if (tileMode >= 0) {
            Shader.TileMode mode = parseTileMode(tileMode);
            mConfig.tileModeX = mode;
            mConfig.tileModeY = mode;
        }

        a.recycle();

        applyConfig(true);
    }

    private void readCorner(TypedArray a, int index, int attr) {
        float r = a.getDimensionPixelSize(attr, -1);
        mConfig.cornerRadii[index] = Math.max(0, r);
    }

    private static Shader.TileMode parseTileMode(int value) {
        switch (value) {
            case 0:
                return Shader.TileMode.CLAMP;
            case 1:
                return Shader.TileMode.REPEAT;
            case 2:
                return Shader.TileMode.MIRROR;
        }
        return DEFAULT_TILE_MODE;
    }

    // ===================== 核心应用逻辑 =====================

    private void applyConfig(boolean applyBackground) {
        applyToDrawable(mDrawable, mConfig.scaleType);

        if (applyBackground && mMutateBackground) {
            applyToDrawable(mBackgroundDrawable, ScaleType.FIT_XY);
        }

        invalidate();
    }

    private void applyToDrawable(Drawable drawable, ScaleType scaleType) {
        if (drawable == null) return;

        if (drawable instanceof RoundedDrawable) {
            RoundedDrawable rd = (RoundedDrawable) drawable;
            rd.setScaleType(scaleType)
                    .setBorderWidth(mConfig.borderWidth)
                    .setBorderColor(mConfig.borderColor)
                    .setOval(mConfig.isOval)
                    .setTileModeX(mConfig.tileModeX)
                    .setTileModeY(mConfig.tileModeY)
                    .setCornerRadius(
                            mConfig.cornerRadii[RoundedCorner.TOP_LEFT],
                            mConfig.cornerRadii[RoundedCorner.TOP_RIGHT],
                            mConfig.cornerRadii[RoundedCorner.BOTTOM_RIGHT],
                            mConfig.cornerRadii[RoundedCorner.BOTTOM_LEFT]
                    );

            if (mHasColorFilter) {
                rd.setColorFilter(mColorFilter);
            }
        } else if (drawable instanceof LayerDrawable) {
            LayerDrawable ld = (LayerDrawable) drawable;
            for (int i = 0; i < ld.getNumberOfLayers(); i++) {
                applyToDrawable(ld.getDrawable(i), scaleType);
            }
        }
    }

    // ===================== ImageView 覆写 =====================

    @Override
    public void setScaleType(ScaleType scaleType) {
        if (scaleType == null || scaleType == mConfig.scaleType) return;

        mConfig.scaleType = scaleType;

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

        applyConfig(false);
    }

    @Override
    public ScaleType getScaleType() {
        return mConfig.scaleType;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        mImageResId = 0;
        mDrawable = RoundedDrawable.fromDrawable(drawable);
        applyConfig(false);
        super.setImageDrawable(mDrawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        setImageDrawable(RoundedDrawable.fromBitmap(bm));
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        if (mImageResId == resId) return;
        mImageResId = resId;
        Drawable d = resolveResource(resId);
        setImageDrawable(d);
    }

    @Override
    public void setImageURI(Uri uri) {
        super.setImageURI(uri);
        setImageDrawable(getDrawable());
    }

    private Drawable resolveResource(int resId) {
        try {
            return RoundedDrawable.fromDrawable(getResources().getDrawable(resId));
        } catch (Exception e) {
            Log.w(TAG, "Unable to load image resource: " + resId, e);
            return null;
        }
    }

    // ===================== public API 保持原样（省略未改部分） =====================
    // setCornerRadius / setBorderWidth / setOval / setTileModeX/Y / mutateBackground 等
    // → 内部仅改为 mConfig + applyConfig()

}
