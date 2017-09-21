package view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by chenglin on 2017-7-14.
 */

public class WebImageView extends AppCompatImageView {
    //    private static final int CACHE_SIZE = (int) (Runtime.getRuntime().maxMemory() / 1024 / 10); //取可用内存的十分之一，单位KB
//    private static final LruCache<String, GifDrawable> mLruCache = new LruCache<String, GifDrawable>(CACHE_SIZE) {
//        protected int sizeOf(String key, GifDrawable gifDrawable) {
//            //计算单个gifDrawable的内存占用，单位KB
//            Log.v("tag_2","" + (int) (gifDrawable.getAllocationByteCount() / 1024));
//            return (int) (gifDrawable.getAllocationByteCount() / 1024);
//        }
//    };

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void load(String url, int imageWidth, int imageHeight) {
        Picasso.with(getContext())
                .load(url)
                .centerCrop()
                .resize(imageWidth, imageHeight)
                .into(this);
    }
}
