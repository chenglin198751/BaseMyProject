package com.wcl.test.view.round;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bumptech.glide.request.RequestOptions;
import com.wcl.test.GlideApp;

public class GlideRoundedImageView extends RoundedImageView {

    public GlideRoundedImageView(Context context) {
        super(context);
    }

    public GlideRoundedImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GlideRoundedImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 使用 Glide 加载网络或本地图片
     *
     * @param uri 图片 URL、文件路径或资源 ID
     */
    public void loadImage(Object uri) {
        RequestOptions options = new RequestOptions();

        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            options = options.override(width, height);
        }

        GlideApp.with(getContext())
                .load(uri)
                .apply(options)
                .into(this);
    }
}
