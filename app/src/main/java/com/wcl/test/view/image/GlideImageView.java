package com.wcl.test.view.image;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.wcl.test.GlideApp;

/**
 * 基于 Glide 的图片加载控件，继承自 {@link RoundedBgImageView}，
 * 支持圆角、圆形、边框、宽高比等特性。
 *
 * <h3>XML 属性</h3>
 * <ul>
 *   <li>{@code riv_corner_radius} — 圆角半径（dp）</li>
 *   <li>{@code riv_oval} — 是否为圆形（true/false）</li>
 *   <li>{@code riv_aspect_ratio} — 宽高比（宽/高，如 1.5 表示宽是高的 1.5 倍）</li>
 *   <li>{@code riv_border_width} — 边框宽度（dp）</li>
 *   <li>{@code riv_border_color} — 边框颜色</li>
 *   <li>{@code riv_solid_color} — 背景填充色</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 *
 * <b>1. XML 布局 — 基础圆角图片</b>
 * <pre>{@code
 * <com.wcl.test.view.image.GlideImageView
 *     android:id="@+id/ivAvatar"
 *     android:layout_width="100dp"
 *     android:layout_height="100dp"
 *     app:riv_corner_radius="8dp" />
 * }</pre>
 *
 * <b>2. XML 布局 — 圆形头像 + 边框</b>
 * <pre>{@code
 * <com.wcl.test.view.image.GlideImageView
 *     android:id="@+id/ivCircleAvatar"
 *     android:layout_width="80dp"
 *     android:layout_height="80dp"
 *     app:riv_oval="true"
 *     app:riv_border_width="2dp"
 *     app:riv_border_color="#FF4081" />
 * }</pre>
 *
 * <b>3. XML 布局 — 固定宽高比（如 16:9 横图）</b>
 * <pre>{@code
 * <com.wcl.test.view.image.GlideImageView
 *     android:id="@+id/ivBanner"
 *     android:layout_width="match_parent"
 *     android:layout_height="wrap_content"
 *     app:riv_aspect_ratio="1.778"
 *     app:riv_corner_radius="12dp" />
 * }</pre>
 */
public class GlideImageView extends RoundedBgImageView {
    private static final int PLACEHOLDER_COLOR = 0x99e8e8e8;

    private Drawable getPlaceholder() {
        if (cornerRadius > 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(PLACEHOLDER_COLOR);
            drawable.setCornerRadius(cornerRadius);
            return drawable;
        }
        return new ColorDrawable(PLACEHOLDER_COLOR);
    }

    public GlideImageView(Context context) {
        super(context);
    }

    public GlideImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GlideImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 使用 Glide 加载网络或本地图片
     *
     * @param uri 图片 URL、文件路径或资源 ID
     */
    public void loadImage(Object uri) {
        RequestOptions options = new RequestOptions();

        // 仅在 cornerRadius > 0 时添加圆角裁剪
        if (cornerRadius > 0) {
            options = options.transform(new CenterCrop(), new RoundedCorners((int) cornerRadius));
        } else {
            options = options.transform(new CenterCrop());
        }

        loadImage(uri, options);
    }


    public void loadImage(Object uri, RequestOptions options) {
        Drawable ph = getPlaceholder();
        GlideApp.with(this)
                .load(uri)
                .apply(options)
                .placeholder(ph)
                .error(ph)
                .into(this);
    }

}
