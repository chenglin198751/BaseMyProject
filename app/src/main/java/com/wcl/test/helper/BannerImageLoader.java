package com.wcl.test.helper;

import android.content.Context;
import android.widget.ImageView;

import com.wcl.test.utils.Constants;
import com.wcl.test.utils.SmartImageLoader;

/**
 * Created by chenglin on 2017-9-27.
 */

public class BannerImageLoader extends com.youth.banner.loader.ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        SmartImageLoader.load(imageView, path, Constants.screenWidth, Constants.screenWidth / 2, 0);
    }

    @Override
    public ImageView createImageView(Context context) {
        return new ImageView(context);
    }
}
