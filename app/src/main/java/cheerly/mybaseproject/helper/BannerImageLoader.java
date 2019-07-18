package cheerly.mybaseproject.helper;

import android.content.Context;
import android.widget.ImageView;

import cheerly.mybaseproject.utils.Constants;
import cheerly.mybaseproject.utils.SmartImageLoader;

/**
 * Created by chenglin on 2017-9-27.
 */

public class BannerImageLoader extends com.youth.banner.loader.ImageLoader {
    @Override
    public void displayImage(Context context, Object path, ImageView imageView) {
        SmartImageLoader.getInstance().load(imageView, (String) path, Constants.getScreenWidth(), Constants.getScreenWidth() / 2);
    }

    @Override
    public ImageView createImageView(Context context) {
        return new ImageView(context);
    }
}
