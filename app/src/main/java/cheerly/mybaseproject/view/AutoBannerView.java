package cheerly.mybaseproject.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.utils.BaseUtils;
import cheerly.mybaseproject.utils.SmartImageLoader;

public class AutoBannerView extends RelativeLayout {
    private ViewPager mViewPager;
    private List<BannerDataItem> mDataList = new ArrayList<>();
    private BannerAdapter mAdapter;
    private Timer mTimer;
    private boolean isFinish = false;
    private boolean isAutoPlay = true;

    public AutoBannerView(Context context) {
        super(context);
        init();
    }

    public AutoBannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setDataList(List<BannerDataItem> dataList) {
        mDataList = dataList;
    }

    private void init() {
        AutoBannerView.this.setClipChildren(false);
        mViewPager = new ViewPager(getContext());
        mViewPager.setPageMargin(-BaseUtils.dip2px(24f));
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageTransformer(false, new ScaleTransformer());

        mViewPager.setClipChildren(false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -2);
        params.leftMargin = BaseUtils.dip2px(20f);
        params.rightMargin = BaseUtils.dip2px(20f);
        addView(mViewPager, params);

        mAdapter = new BannerAdapter(getContext());
        mViewPager.setAdapter(mAdapter);

        startTimer(new Runnable() {
            @Override
            public void run() {
                if (isAutoPlay){
                    mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                }
            }
        });
    }


    public void onDestroy() {
        isFinish = true;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_OUTSIDE) {
            isAutoPlay = true;
        } else if (action == MotionEvent.ACTION_DOWN) {
            isAutoPlay = false;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void startTimer(final Runnable runnable) {
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                if (!isFinish) {
                    BaseUtils.getHandler().post(runnable);
                } else {
                    cancel();
                    mTimer.cancel();
                    mTimer = null;
                }
            }
        }, 5 * 1000, 5 * 1000);
    }

    private class BannerAdapter extends PagerAdapter {
        private Context mContext;

        private BannerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_vp, null);
            int newPos = position % (mDataList.size());
            ImageView img = view.findViewById(R.id.image);
            SmartImageLoader.getInstance().load(img, mDataList.get(newPos).url, -1, -1);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }

    public static final class BannerDataItem {
        public String url;
    }


    public class ScaleTransformer implements ViewPager.PageTransformer {
        private final float MINSCALE = 0.8f;//最小缩放值

        /**
         * position取值特点：
         * 假设页面从0～1，则：
         * 第一个页面position变化为[0,-1]
         * 第二个页面position变化为[1,0]
         *
         * @param view
         * @param v
         */
        @Override
        public void transformPage(@NonNull View view, float v) {

            float scale;
            if (v > 1 || v < -1) {
                scale = MINSCALE;
            } else if (v < 0) {
                scale = MINSCALE + (1 + v) * (1 - MINSCALE);
            } else {
                scale = MINSCALE + (1 - v) * (1 - MINSCALE);
            }
            view.setScaleY(scale);
            view.setScaleX(scale);
        }
    }
}
