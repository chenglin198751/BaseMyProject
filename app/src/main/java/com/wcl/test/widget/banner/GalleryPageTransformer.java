package com.wcl.test.widget.banner;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class GalleryPageTransformer implements ViewPager2.PageTransformer {

    @Override
    public void transformPage(@NonNull View page, float position) {
        float scale = 0.85f + (1 - Math.abs(position)) * 0.15f;
        page.setScaleY(scale);
        page.setScaleX(scale);

        float alpha = 0.5f + (1 - Math.abs(position)) * 0.5f;
        page.setAlpha(alpha);
    }
}