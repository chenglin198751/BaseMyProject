package com.wcl.test.view.image;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.wcl.test.GlideApp;

public class GlideBgImageView extends RoundedBgImageView {

    public GlideBgImageView(Context context) {
        super(context);
    }

    public GlideBgImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GlideBgImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 使用 Glide 加载网络或本地图片
     *
     * @param uri 图片 URL、文件路径或资源 ID
     */
    public void loadImage(Object uri) {
        RequestOptions options = new RequestOptions();

        // 如果 View 已经测量完，指定尺寸，优化 Glide 解码
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0) {
            options = options.override(width, height);
        }

        // 仅在 cornerRadius > 0 时添加圆角裁剪
        if (cornerRadius > 0) {
            options = options.transform(new CenterCrop(), new RoundedCorners((int) cornerRadius));
        } else {
            options = options.transform(new CenterCrop());
        }

        loadImage(uri, options);
    }


    public void loadImage(Object uri, RequestOptions options) {
//        ColorDrawable d = new ColorDrawable(0x99e8e8e8);
        ColorDrawable d = new ColorDrawable(Color.BLUE);
        GlideApp.with(this)
                .load(uri)
                .apply(options)
                .placeholder(d)
                .error(d)
                .into(this);
    }

}
