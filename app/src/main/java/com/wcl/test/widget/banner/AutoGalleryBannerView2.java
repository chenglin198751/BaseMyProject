package com.wcl.test.widget.banner;

import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.wcl.test.utils.AppBaseUtils;

import java.util.ArrayList;
import java.util.List;



//List<String> imageList = Arrays.asList(
//        "https://qd.shouji.qihucdn.com/media/7596e61dd2bc80488dbca79665ec1252/660127d7974f7.png",
//        "https://d02.qd.shouji.360tpcdn.com/media/3768e5340f2139e71661b805718e4cce/67d3e3a7d7717.png",
//        "https://qd.shouji.qihucdn.com/media/80d15cfc4174f0bb48e9231400160487/6602aa5c7dfde.png",
//        "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png",
//        "https://qd.shouji.qihucdn.com/media/3471cdbe7ce5812df964fbd68226edc0/6602aa4ad6b7f.png"
//);
//
//
//AutoGalleryBannerView2 autoGalleryBannerView2 = new AutoGalleryBannerView2(mViewBinding.bannerViewPager);
//        autoGalleryBannerView2.setDataList(imageList);
//        autoGalleryBannerView2.setItemMargin(-10);


public class AutoGalleryBannerView2 implements DefaultLifecycleObserver {
    private final ViewPager2 bannerViewPager;
    private List<String> imageList = new ArrayList<>();
    //    private final ViewPager2SlowScrollHelper slowScrollHelper;
    private boolean isAutoScrolling = true;
    private int itemSpacing = -AppBaseUtils.dp2px(5);
    private static final int AUTO_SCROLL_DELAY = 3000; // 自动滚动延迟时间，单位毫秒
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrolling) {
                int currentItem = bannerViewPager.getCurrentItem();
                bannerViewPager.setCurrentItem(currentItem + 1);
//                slowScrollHelper.setCurrentItem(currentItem + 1);
                handler.postDelayed(this, AUTO_SCROLL_DELAY);
            }
        }
    };


    public AutoGalleryBannerView2(ViewPager2 viewPager2) {
        bannerViewPager = viewPager2;

        FragmentActivity activity = (FragmentActivity) AppBaseUtils.getActivityFromContext(bannerViewPager.getContext());
        if (activity != null) {
            activity.getLifecycle().addObserver(this);
        }

        // 创建ViewPager2SlowScrollHelper实例，设置滚动时长
//        slowScrollHelper = new ViewPager2SlowScrollHelper(bannerViewPager, 500);

        bannerViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

        // 设置左右两侧都可以预览
        bannerViewPager.setOffscreenPageLimit(3);
        RecyclerView recyclerView = (RecyclerView) bannerViewPager.getChildAt(0);
        recyclerView.setClipToPadding(false);
        recyclerView.setClipChildren(false);


        bannerViewPager.setPageTransformer(new GalleryPageTransformer());


        setItemMargin(-5);

        // 设置监听器，实现无缝循环
        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageSelected(int position) {
//                if (position == 0) {
//                    int newPosition = Integer.MAX_VALUE - imageList.size();
//                    bannerViewPager.setCurrentItem(newPosition, false);
//                } else if (position == Integer.MAX_VALUE - 1) {
//                    bannerViewPager.setCurrentItem(imageList.size(), false);
//                }
//            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
                    // 用户开始拖动，停止自动滚动
                    isAutoScrolling = false;
                    handler.removeCallbacks(autoScrollRunnable);
                } else if (state == ViewPager2.SCROLL_STATE_IDLE && !isAutoScrolling) {
                    // 用户停止拖动，恢复自动滚动
                    isAutoScrolling = true;
                    handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
                }
            }
        });

        // 开始自动滚动
        if (isAutoScrolling) {
            handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
        }
    }


    public void setDataList(List<String> list) {
        this.imageList = list;

        BannerAdapter adapter = new BannerAdapter(imageList);
        bannerViewPager.setAdapter(adapter);

        // 设置初始位置为中间位置
        int initialPosition = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % imageList.size());
        bannerViewPager.setCurrentItem(initialPosition, false);

    }

    public void setCurrentItemWithScrollingSpeed() {

    }

    /**
     * 设置item之间的左右间距，负数是缩小间距，单位是dp
     */
//    public void setItemMargin(int spacing) {
//        this.itemSpacing = AppBaseUtils.dip2px(spacing);
//        // 添加 item 间距（可选）
//        bannerViewPager.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect,
//                                       @NonNull View view,
//                                       @NonNull RecyclerView parent,
//                                       @NonNull RecyclerView.State state) {
//                int position = parent.getChildAdapterPosition(view);
//                int itemCount = state.getItemCount();
//
//                // 设置左右两侧的间距为10dp
//                if (position == 0) {
//                    // 第一个item，只设置右边间距
//                    outRect.right = itemSpacing;
//                } else if (position == itemCount - 1) {
//                    // 最后一个item，只设置左边间距
//                    outRect.left = itemSpacing;
//                } else {
//                    // 中间的item，左右都设置间距
//                    outRect.left = itemSpacing;
//                    outRect.right = itemSpacing;
//                }
//            }
//        });
//    }

    private RecyclerView.ItemDecoration currentItemDecoration;

    public void setItemMargin(int spacing) {
        this.itemSpacing = AppBaseUtils.dp2px(spacing);

        RecyclerView recyclerView = (RecyclerView) bannerViewPager.getChildAt(0);

        // 移除旧的 ItemDecoration（如果存在）
        if (currentItemDecoration != null) {
            recyclerView.removeItemDecoration(currentItemDecoration);
        }

        // 创建并添加新的 ItemDecoration
        currentItemDecoration = new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect,
                                       @NonNull View view,
                                       @NonNull RecyclerView parent,
                                       @NonNull RecyclerView.State state) {
                int position = parent.getChildAdapterPosition(view);
                int itemCount = state.getItemCount();

                if (position == 0) {
                    outRect.right = itemSpacing;
                } else if (position == itemCount - 1) {
                    outRect.left = itemSpacing;
                } else {
                    outRect.left = itemSpacing;
                    outRect.right = itemSpacing;
                }
            }
        };

        recyclerView.addItemDecoration(currentItemDecoration);
    }

    public void setAutoScrolling(boolean isAuto){
        isAutoScrolling = isAuto;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        DefaultLifecycleObserver.super.onDestroy(owner);
    }
}
