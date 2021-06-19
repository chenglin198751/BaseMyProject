package com.wcl.test.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Keep;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import com.wcl.test.GlideApp;
import com.wcl.test.GlideRequest;
import com.wcl.test.R;
import com.wcl.test.view.CenterDrawable;

/**
 * Created by chenglin on 2017-7-14.
 */
@Keep
public class SmartImageLoader {
    private SmartImageLoader() {
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是px.
     * 如果 radius <0  , 那么就是纯圆圈图片;
     * 如果 radius = 0 , 那么就是直角图片;
     * 如果 radius >0 , 是圆角图片
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     * 注意：图片默认显示类型为 CENTER_CROP
     */
    public static void load(ImageView imageView,
                            Object object,
                            int imageWidth,
                            int imageHeight,
                            int radius) {

        ImageLoaderBuilder builder = new ImageLoaderBuilder();
        builder.setUri(object);
        builder.setImageWidth(imageWidth);
        builder.setImageHeight(imageHeight);

        if (radius == 0) {
            builder.setScaleType(ImageLoaderBuilder.CENTER_CROP);
            builder.setRadius(CenterDrawable.RECTANGLE);
        } else if (radius > 0){
            builder.setScaleType(ImageLoaderBuilder.ROUNDED);
            builder.setRadius(radius);
        } else {
            builder.setScaleType(ImageLoaderBuilder.CIRCLE_CROP);
            builder.setRadius(radius);
        }
        load(imageView, builder);
    }

    public static void load(ImageView imageView, ImageLoaderBuilder builder) {
        loadRound(imageView,
                builder.getUri(),
                builder.getImageWidth(),
                builder.getImageHeight(),
                builder.getRadius(),
                builder.getPlaceholder(),
                builder.getScaleType());
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是px.
     * 如果 radius <0  , 那么就是纯圆圈图片;
     * 如果 radius = 0 , 那么就是直角图片;
     * 如果 radius >0 , 是圆角图片
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     *
     * @param imageView   ImageView
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度(单位px)
     * @param imageHeight 图片的高度(单位px)
     * @param radius      图片的圆角角度(单位px)
     * @param placeholder 占位图，如果不设置，也会有默认占位图
     */
    private static void loadRound(ImageView imageView,
                                  Object object,
                                  int imageWidth,
                                  int imageHeight,
                                  int radius,
                                  final Drawable placeholder,
                                  final int scaleType) {
        try {
            loadRound2(imageView, object, imageWidth, imageHeight, radius, placeholder, scaleType);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void loadRound2(ImageView imageView,
                                   Object object,
                                   int imageWidth,
                                   int imageHeight,
                                   int radius,
                                   final Drawable placeholder,
                                   final int scaleType) {

        Activity activity = BaseUtils.getActivityFromContext(imageView.getContext());
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return;
        }

        Drawable centerDrawable = new CenterDrawable(R.drawable.image_loadding_icon, radius);
        if (placeholder != null) {
            centerDrawable = placeholder;
        }

        if (object == null) {
            imageView.setImageDrawable(centerDrawable);
            return;
        }

        if (object instanceof String) {
            String url = (String) object;
            if (TextUtils.isEmpty(url)) {
                imageView.setImageDrawable(centerDrawable);
                return;
            }
        }

        GlideRequest glideRequest = GlideApp
                .with(imageView.getContext())
                .applyDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                .load(object)
                .placeholder(centerDrawable)
                .error(centerDrawable);

        if (imageWidth > 0 && imageHeight > 0) {
            glideRequest = glideRequest.override(imageWidth, imageHeight);
        }

        if (scaleType == ImageLoaderBuilder.CENTER_CROP) {
            glideRequest = glideRequest.centerCrop();
        } else if (scaleType == ImageLoaderBuilder.CENTER_INSIDE) {
            glideRequest = glideRequest.centerInside();
        } else if (scaleType == ImageLoaderBuilder.FIT_CENTER) {
            glideRequest = glideRequest.fitCenter();
        } else if (scaleType == ImageLoaderBuilder.CIRCLE_CROP) {
            glideRequest = glideRequest.circleCrop();
        } else if (scaleType == ImageLoaderBuilder.ROUNDED) {
            RoundedCorners roundedCorner = new RoundedCorners(radius);
            glideRequest = glideRequest.transform(new CenterCrop(), roundedCorner);
        }

        glideRequest.into(imageView);
    }
}
