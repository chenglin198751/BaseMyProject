package cheerly.mybaseproject.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Keep;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import cheerly.mybaseproject.GlideApp;
import cheerly.mybaseproject.GlideRequest;
import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;

/**
 * Created by chenglin on 2017-7-14.
 */
@SuppressLint("AppCompatCustomView")
@Keep
public class WebImageView extends ImageView {

    public WebImageView(Context context) {
        super(context);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 加载图片，一定要传入 ImageView 的宽和高，因为这样可以很大的节约内存
     * 支持 gif 格式的图片，但是前提后缀名是.gif 才能解析
     * 如果图片宽度和高度都设置为-1 ，那么就是加载原图。不推荐，因为原图如果太大，很耗费内存。不过某种情况下确实需要加载原图
     *
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度
     * @param imageHeight 图片的高度
     */
    public void load(Object object, int imageWidth, int imageHeight) {
        loadRound(object, imageWidth, imageHeight, CenterDrawable.RECTANGLE);
    }

    /**
     * 加载图片，一定要传入 ImageView 的宽和高，因为这样可以很大的节约内存
     * 支持 gif 格式的图片，但是前提后缀名是.gif 才能解析
     * 如果图片宽度和高度都设置为-1 ，那么就是加载原图。不推荐，因为原图如果太大，很耗费内存。不过某种情况下确实需要加载原图
     *
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度
     * @param imageHeight 图片的高度
     * @param resDrawable 占位图，如果不设置，也会有默认占位图
     */
    public void load(Object object, int imageWidth, int imageHeight, @DrawableRes int resDrawable) {
        loadRound(object, imageWidth, imageHeight, CenterDrawable.RECTANGLE, resDrawable);
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是dp.
     * 如果 radius <0  , 那么就是纯圆圈图片;
     * 如果 radius = 0 , 那么就是直角图片;
     * 如果 radius >0 , 是圆角图片
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     *
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度
     * @param imageHeight 图片的高度
     * @param radius      图片的圆角角度，传入的单位是dp
     */
    public void loadRound(Object object, int imageWidth, int imageHeight, int radius) {
        loadRound(object, imageWidth, imageHeight, radius, 0);
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是dp.
     * 如果 radius <0  , 那么就是纯圆圈图片;
     * 如果 radius = 0 , 那么就是直角图片;
     * 如果 radius >0 , 是圆角图片
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     *
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度
     * @param imageHeight 图片的高度
     * @param radius      图片的圆角角度，传入的单位是dp
     * @param resDrawable 占位图，如果不设置，也会有默认占位图
     */
    public void loadRound(Object object, int imageWidth, int imageHeight, int radius, int resDrawable) {
        loadRound(object, imageWidth, imageHeight, radius, resDrawable <= 0 ? null : getResources().getDrawable(resDrawable));
    }

    /**
     * 加载图片使其变为圆角或者圆形，radius传入的单位是dp.
     * 如果 radius <0  , 那么就是纯圆圈图片;
     * 如果 radius = 0 , 那么就是直角图片;
     * 如果 radius >0 , 是圆角图片
     * 如果imageWidth = -1 && imageHeight == -1 ，就是加载原图
     *
     * @param object      图片地址Url、图片文件file
     * @param imageWidth  图片的宽度
     * @param imageHeight 图片的高度
     * @param radius      图片的圆角角度，传入的单位是dp
     * @param drawable    占位图，如果不设置，也会有默认占位图
     */
    public void loadRound(Object object, int imageWidth, int imageHeight, int radius, final Drawable drawable) {
        Drawable centerDrawable = new CenterDrawable(R.drawable.image_loadding_icon, radius > 0 ? BaseUtils.dip2px(radius) : radius);
        if (drawable != null) {
            centerDrawable = drawable;
        }

        if (object == null) {
            setImageDrawable(centerDrawable);
            return;
        }

        if (object instanceof String) {
            String url = (String) object;
            if (TextUtils.isEmpty(url)) {
                setImageDrawable(centerDrawable);
                return;
            }
        }

        GlideRequest glideRequest = GlideApp
                .with(getContext())
                .applyDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_ARGB_8888))
                .load(object)
                .placeholder(centerDrawable)
                .error(centerDrawable);

        if (imageWidth > 0 && imageHeight > 0) {
            glideRequest = glideRequest.override(imageWidth, imageHeight);
        }

        if (radius == CenterDrawable.RECTANGLE) {
            glideRequest = glideRequest.centerCrop();
        } else if (radius < 0) {
            glideRequest = glideRequest.circleCrop();
        } else {
            RoundedCorners roundedCorner = new RoundedCorners(BaseUtils.dip2px(radius));
            glideRequest = glideRequest.transforms(new CenterCrop(), roundedCorner);
        }
        glideRequest.into(this);
    }

}
