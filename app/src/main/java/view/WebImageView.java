package view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by chenglin on 2017-7-14.
 */

public class WebImageView extends ImageView {
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
