package com.wcl.test.utils;

import android.graphics.Bitmap;
import android.view.View;
import com.wcl.test.listener.OnFinishedListener;

/**
 * java 原生api 实现的
 * Stack Blur v1.0 from http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
 * Java Author: Mario Klingemann <mario at quasimondo.com>
 * Android port : Yahel Bouaziz <yahel at kayenko.com>
 * <p>
 * Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>
 */
public class FastBlurUtil2 {

    /**
     * 对View进行高斯模糊，异步返回Bitmap
     * @param view   要被高斯模糊的View 生成的图片
     * @param scale  对原图的缩放比例：0.1~1，建议大图0.1~0.2，小图0.2~0.4
     * @param radius 高斯模糊程度：1~100，建议10~20
     * @param listener 结果回调
     */
    public static void doBlur(final View view, final float scale, final int radius,
                              final OnFinishedListener<Boolean, Bitmap> listener) {
        final Bitmap sentBitmap = BitmapUtils.createBitmapFromView(view);
        new Thread(() -> {
            Bitmap bitmap = doBlur(sentBitmap, scale, radius);
            AppBaseUtils.getUiHandler().post(() -> {
                if (listener != null) {
                    listener.onFinished(true, bitmap);
                }
            });
        }).start();
    }

    /**
     * 对Bitmap执行高斯模糊
     * @param sentBitmap 原始Bitmap
     * @param scale      缩放比例
     * @param radius     模糊半径
     * @return 模糊后Bitmap
     */
    public static Bitmap doBlur(Bitmap sentBitmap, float scale, final int radius) {
        if (sentBitmap == null) return null;
        if (scale <= 0f || scale > 1f) scale = 1f;
        if (radius < 1) return sentBitmap;

        int srcWidth = sentBitmap.getWidth();
        int srcHeight = sentBitmap.getHeight();
        int scaledWidth = Math.max(1, Math.round(srcWidth * scale));
        int scaledHeight = Math.max(1, Math.round(srcHeight * scale));
        Bitmap bitmap = Bitmap.createScaledBitmap(sentBitmap, scaledWidth, scaledHeight, true);

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1, hm = h - 1, wh = w * h, div = radius + radius + 1;
        int[] r = new int[wh], g = new int[wh], b = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int[] vmin = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[256 * divsum];
        for (i = 0; i < dv.length; i++) dv[i] = (i / divsum);

        int[][] stack = new int[div][3];
        int stackpointer, stackstart;
        int[] sir;
        int rbs, r1 = radius + 1;
        int routsum, goutsum, boutsum, rinsum, ginsum, binsum;

        yw = yi = 0;
        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p >> 16) & 0xff;
                sir[1] = (p >> 8) & 0xff;
                sir[2] = p & 0xff;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = (stackpointer - radius + div) % div;
                sir = stack[stackstart];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) vmin[x] = Math.min(x + r1, wm);
                p = pix[yw + vmin[x]];

                sir[0] = (p >> 16) & 0xff;
                sir[1] = (p >> 8) & 0xff;
                sir[2] = p & 0xff;

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) yp += w;
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = (stackpointer - radius + div) % div;
                sir = stack[stackstart];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) vmin[y] = Math.min(y + r1, hm) * w;
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return bitmap;
    }
}