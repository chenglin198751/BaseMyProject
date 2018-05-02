package helper;

import android.content.Context;
import android.widget.ImageView;

import com.youth.banner.loader.ImageLoader;

import utils.Constants;
import view.WebImageView;

/**
 * Created by chenglin on 2017-9-27.
 */

public class BannerImageLoader extends ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        if (imageView instanceof WebImageView) {
            WebImageView webImageView = (WebImageView) imageView;
            webImageView.load((String) path, Constants.getScreenWidth(), Constants.getScreenWidth() / 2);
        }
    }

    @Override
    public ImageView createImageView(Context context) {
        return new WebImageView(context);
    }
}
