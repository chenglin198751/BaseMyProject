package com.wcl.test.utils;

import android.graphics.drawable.Drawable;

import com.wcl.test.base.BaseApp;
import com.wcl.test.view.CenterDrawable;

public class ImageLoaderBuilder {
    public static final int FIT_CENTER = 0;
    public static final int CENTER_CROP = 1;
    public static final int CENTER_INSIDE = 2;
    public static final int CIRCLE_CROP = 3;
    public static final int ROUNDED = 4;

    private Object uri = null;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int radius = CenterDrawable.RECTANGLE;
    private int placeholder = -1;
    private int scaleType = -1;

    public Object getUri() {
        return uri;
    }

    public void setUri(Object uri) {
        this.uri = uri;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Drawable getPlaceholder() {
        if (placeholder <= 0) {
            return null;
        } else {
            return BaseApp.getApp().getResources().getDrawable(placeholder);
        }
    }

    public void setPlaceholder(int placeholder) {
        this.placeholder = placeholder;
    }

    public int getScaleType() {
        return scaleType;
    }

    public void setScaleType(int scaleType) {
        this.scaleType = scaleType;
    }
}
