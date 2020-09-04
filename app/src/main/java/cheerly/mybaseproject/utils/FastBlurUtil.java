package cheerly.mybaseproject.utils;

import android.graphics.Bitmap;
import android.view.View;

import com.dasu.blur.BlurConfig;
import com.dasu.blur.DBlur;
import com.dasu.blur.OnBlurListener;

import cheerly.mybaseproject.base.BaseApp;
import cheerly.mybaseproject.listener.OnFinishListener;

public class FastBlurUtil {

    /**
     * @param view   要被高斯模糊的View
     * @param scale  对原图的缩放比例，默认是8：比如传8就是把原图缩放八分之一的意思。值越大越模糊，耗时越短。
     * @param radius 高斯模糊的半径，默认是4：高斯模糊计算半径，半径越大越模糊，但通常越耗时
     */
    public static void doBlur(View view, int scale, final int radius, final OnFinishListener<Bitmap> listener) {
        DBlur.source(view).mode(BlurConfig.MODE_NATIVE).sampling(scale).radius(radius).build()
                .doBlur(new OnBlurListener() {
                    @Override
                    public void onBlurSuccess(Bitmap bitmap) {
                        listener.onFinish(bitmap);
                    }

                    @Override
                    public void onBlurFailed() {
                        listener.onFinish(null);
                    }
                });
    }


    /**
     * @param sentBitmap 要被高斯模糊的Bitmap
     * @param scale      对原图的缩放比例，默认是8：比如传8就是把原图缩放八分之一的意思。值越大越模糊，耗时越短。
     * @param radius     高斯模糊的半径，默认是4：高斯模糊计算半径，半径越大越模糊，但通常越耗时
     */
    public static void doBlur(Bitmap sentBitmap, int scale, final int radius, final OnFinishListener<Bitmap> listener) {
        DBlur.source(BaseApp.getApp(), sentBitmap).mode(BlurConfig.MODE_NATIVE).sampling(scale).radius(radius).build()
                .doBlur(new OnBlurListener() {
                    @Override
                    public void onBlurSuccess(Bitmap bitmap) {
                        listener.onFinish(bitmap);
                    }

                    @Override
                    public void onBlurFailed() {
                        listener.onFinish(null);
                    }
                });
    }

}