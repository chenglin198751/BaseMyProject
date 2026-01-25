package com.wcl.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.wcl.test.R;
import com.wcl.test.utils.AppBaseUtils;
import com.wcl.test.view.image.GlideImageView;

public class AutoGalleryBannerView extends RelativeLayout implements DefaultLifecycleObserver {
    private static final long AUTO_PLAY_INTERVAL = 5000L;
    private static final float MIN_SCALE = 0.8f;

    private ViewPager mViewPager;
    private List<BannerDataItem> mDataList = new ArrayList<>();
    private BannerAdapter mAdapter;
    private Timer mTimer;
    private boolean isFinish = false;
    private boolean isAutoPlay = true;

    public AutoGalleryBannerView(Context context) {
        super(context);
        init();
    }

    public AutoGalleryBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setDataList(List<BannerDataItem> dataList) {
        this.mDataList = dataList != null ? dataList : new ArrayList<>();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void init() {
        FragmentActivity activity = (FragmentActivity) AppBaseUtils.getActivityFromContext(getContext());
        if (activity != null) {
            activity.getLifecycle().addObserver(this);
        }

        setClipChildren(false);
        mViewPager = new ViewPager(getContext());
        mViewPager.setPageMargin(-AppBaseUtils.dp2px(24f));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageTransformer(false, new ScaleTransformer());
        mViewPager.setClipChildren(false);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
        params.leftMargin = AppBaseUtils.dp2px(20f);
        params.rightMargin = AppBaseUtils.dp2px(20f);
        addView(mViewPager, params);

        mAdapter = new BannerAdapter(getContext());
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        isFinish = false;
        startTimer(() -> {
            if (isAutoPlay && mViewPager != null) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            }
        });
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        isFinish = true;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        isAutoPlay = false;
    }

    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        isAutoPlay = true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                isAutoPlay = false;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                isAutoPlay = true;
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void startTimer(final Runnable runnable) {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isFinish) {
                    AppBaseUtils.getUiHandler().post(runnable);
                } else {
                    cancel();
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer = null;
                    }
                }
            }
        }, AUTO_PLAY_INTERVAL, AUTO_PLAY_INTERVAL);
    }

    private class BannerAdapter extends PagerAdapter {
        private Context mContext;

        private BannerAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mDataList.isEmpty() ? 0 : Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            if (mDataList.isEmpty()) {
                return new View(mContext); // 返回空视图防止崩溃
            }

            View view = LayoutInflater.from(mContext).inflate(R.layout.auto_gallery_banner_item, container, false);
            int index = position % mDataList.size();
            BannerDataItem item = mDataList.get(index);

            GlideImageView img = view.findViewById(R.id.image);
            ImageView childImg = view.findViewById(R.id.child_img);

            img.loadImage(item.url);
//            setImageBitmap(childImg, item.childUrl);

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            ImageView childImg = view.findViewById(R.id.child_img);
            Glide.with(view).clear(childImg); // 清理 Glide 加载任务
            container.removeView(view);
        }
    }

    public static final class BannerDataItem {
        public String url;
        public String childUrl;
    }

    private static final class ScaleTransformer implements ViewPager.PageTransformer {
        @Override
        public void transformPage(@NonNull View view, float position) {
            View childImg = view.findViewById(R.id.child_img);
            float scale;

            if (position > 1 || position < -1) {
                scale = MIN_SCALE;
            } else if (position < 0) {
                scale = MIN_SCALE + (1 + position) * (1 - MIN_SCALE);
            } else {
                scale = MIN_SCALE + (1 - position) * (1 - MIN_SCALE);
            }

            view.setScaleX(scale);
            view.setScaleY(scale);
            if (childImg != null) {
                childImg.setScaleX(scale);
                childImg.setScaleY(scale);
            }
        }
    }
}
