//package com.wcl.test.main;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.os.CountDownTimer;
//import android.text.TextUtils;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.Keep;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.request.target.SimpleTarget;
//import com.bumptech.glide.request.transition.Transition;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.util.HashMap;
//
//import com.wcl.test.R;
//import com.wcl.test.httpwork.HttpUrls;
//import com.wcl.test.httpwork.HttpUtils;
//import com.wcl.test.listener.OnFinishListener;
//import com.wcl.test.listener.OnSingleClickListener;
//import com.wcl.test.utils.Constants;
//import com.wcl.test.utils.EasyCache;
//import com.wcl.test.utils.SmartImageLoader;
//import com.wcl.test.widget.ToastUtils;
//
//
//public class GameShowAdsViewHelper {
//    private final static float IMG_RATIO = 650f / 375f;
//    private final static String TAG = "show_ads";
//    private final static String KEY_SPLASH_ADS = "KEY_SPLASH_ADS_JSON_DATA";
//    private final static String KEY_SPLASH_DOWNLOADED = "KEY_SPLASH_DOWNLOADED";
//    private Activity mActivity;
//    private ImageView mAdsImg;
//    private TextView mBtnSkip;
//    private View mAdsView;
//    private FrameLayout mRootFrameLayout;
//    private CountDownTimer mCountDownTimer;
//    private int mSkipCount = 3;
//    private OnFinishListener<Boolean> mListener;
//
//    public GameShowAdsViewHelper(Activity activity) {
//        mActivity = activity;
//    }
//
//    public void onDestroy() {
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//        }
//    }
//
//    public void init(OnFinishListener<Boolean> listener) {
//        mListener = listener;
//        getAdsData();
//    }
//
//    private void addView() {
//        mAdsView = View.inflate(mActivity, R.layout.game_show_ads_layout, null);
//        mAdsImg = mAdsView.findViewById(R.id.game_ads_img);
//        mBtnSkip = mAdsView.findViewById(R.id.skip_btn);
//        mAdsView.setClickable(true);
//        mBtnSkip.setVisibility(View.GONE);
//
//        mRootFrameLayout = mActivity.findViewById(android.R.id.content);
//        mRootFrameLayout.addView(mAdsView, new ViewGroup.LayoutParams(-1, -1));
//
//        ViewGroup.LayoutParams params = mAdsImg.getLayoutParams();
//        params.width = Constants.getScreenWidth();
//        params.height = (int) (params.width * IMG_RATIO);
//        mAdsImg.setLayoutParams(params);
//
//        mAdsView.post(new Runnable() {
//            @Override
//            public void run() {
//                showSkipBtn();
//            }
//        });
//
//        mBtnSkip.setOnClickListener(new OnSingleClickListener() {
//            @Override
//            public void onSingleClick(View v) {
//                removeAdsView();
//            }
//        });
//    }
//
//
//    /**
//     * 获取闪屏广告数据
//     */
//    public void getAdsData() {
//        try {
//            //1、只使用网络缓存的数据，如果没有就不用
//            String json = EasyCache.get(mActivity).getAsString(KEY_SPLASH_ADS);
//            if (!TextUtils.isEmpty(json)) {
//                GameAdsModel models = Constants.gson.fromJson(json, GameAdsModel.class);
//                if (models != null && models.normal_ads != null
//                        && models.normal_ads.is_show.equals("1")
//                        && !TextUtils.isEmpty(models.normal_ads.image)
//                        && EasyCache.get(mActivity).getAsString(KEY_SPLASH_DOWNLOADED).equals(models.normal_ads.image)) {
//                    addView();
//                    SmartImageLoader.load(mAdsImg, models.normal_ads.image, -1, -1, 0);
//                    mAdsImg.setOnClickListener(new OnSingleClickListener() {
//                        @Override
//                        public void onSingleClick(View v) {
//                            ToastUtils.show("跳转");
//                            removeAdsView();
//                        }
//                    });
//                } else {
//                    mListener.onFinish(false);
//                }
//            } else {
//                mListener.onFinish(false);
//            }
//
//            //2、网络获取数据，只用来做缓存而不使用
//            HashMap<String, String> hashMap = new HashMap<>();
//            HttpUtils.get(mActivity, HttpUrls.NEWS_CMS_APP_SPLASH_ADS, hashMap, new HttpUtils.HttpCallback() {
//                @Override
//                public void onSuccess(HttpResult result) {
//                    try {
//                        if (mActivity == null || mActivity.isFinishing()) {
//                            return;
//                        } else if (result.errno != 0) {
//                            return;
//                        }
//
//                        //图片接口请求成功缓存数据
//                        EasyCache.get(mActivity).put(KEY_SPLASH_ADS, result.data);
//                        GameAdsModel models = Constants.gson.fromJson(result.data, GameAdsModel.class);
//                        if (models != null && models.normal_ads != null
//                                && models.normal_ads.is_show.equals("1")
//                                && !TextUtils.isEmpty(models.normal_ads.image)) {
//                            Glide.with(mActivity)
//                                    .asBitmap()
//                                    .load(models.normal_ads.image)
//                                    .into(new SimpleTarget<Bitmap>() {
//                                        @Override
//                                        public void onResourceReady(@NotNull Bitmap resource, Transition<? super Bitmap> transition) {
//                                            //图片下载成功后记录
//                                            EasyCache.get(mActivity).put(KEY_SPLASH_DOWNLOADED, models.normal_ads.image);
//                                        }
//                                    });
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onFailure(String url, HttpException e) {
//                }
//            });
//        } catch (Exception e) {
//            mListener.onFinish(false);
//        }
//    }
//
//    private void showSkipBtn() {
//        mBtnSkip.setVisibility(View.VISIBLE);
//        mBtnSkip.setText("跳过 " + mSkipCount);
//
//        mCountDownTimer = new CountDownTimer(3 * 1000, 1000) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                mBtnSkip.setText("跳过 " + mSkipCount);
//                mSkipCount--;
//            }
//
//            @Override
//            public void onFinish() {
//                removeAdsView();
//            }
//        };
//        mCountDownTimer.start();
//    }
//
//
//    private void removeAdsView() {
//        if (mAdsView != null) {
//            mRootFrameLayout.removeView(mAdsView);
//            mAdsView = null;
//            mListener.onFinish(true);
//        }
//
//        if (mCountDownTimer != null) {
//            mCountDownTimer.cancel();
//            mCountDownTimer = null;
//        }
//    }
//
//    @Keep
//    public static class GameAdsModel {
//        public normalAds normal_ads;
//    }
//
//    @Keep
//    public static class normalAds {
//        public String image;
//        public String jump_type;
//        public String jump_data;
//        public String is_show;
//    }
//}
