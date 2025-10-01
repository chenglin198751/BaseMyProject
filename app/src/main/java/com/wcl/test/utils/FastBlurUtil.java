package com.wcl.test.utils;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

import android.widget.ImageView;

import com.wcl.test.GlideApp;

import jp.wasabeef.glide.transformations.BlurTransformation;

//使用Glide图片库实现高斯模糊
public class FastBlurUtil {

    /**
     * @param imageView 图片控件
     * @param width     图片宽
     * @param height    图片高
     * @param imgUrl    图片url或者图片R.id
     * @param radius    模糊半径，值越大越模糊（推荐5-50）
     * @param sampling  采样率，值越大处理速度越快但图片越模糊（推荐1-8）
     */
    public static void doBlur(ImageView imageView, int width, int height, Object imgUrl, int radius, int sampling) {
        GlideApp.with(imageView.getContext())
                .load(imgUrl)
                .apply(bitmapTransform(new BlurTransformation(radius, sampling)))
                .override(width, height)
                .into(imageView);
    }


}