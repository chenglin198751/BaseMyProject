package com.wcl.test.main;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;

import com.wcl.test.R;
import com.wcl.test.base.BaseActivity;
import com.wcl.test.base.BaseFragment;
import com.wcl.test.databinding.ActivityMainBinding;
import com.wcl.test.helper.ShowFragmentHelper;
import com.wcl.test.widget.ToastUtils;
import com.wcl.test.widget.banner.AutoGalleryBannerView2;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    public static final int TAB_FIRST = 0;
    public static final int TAB_SECOND = 1;
    public static final int TAB_THIRD = 2;
    public static final int TAB_FOURTH = 3;
    private boolean isAutoScrolling = true;

    private static final int[] TAB_BOTTOM_ID_ARRAY = {
            R.id.tab_first, R.id.tab_second, R.id.tab_third, R.id.tab_fourth
    };
    private static final int[] TAB_BOTTOM_ICON_ARRAY = {
            R.drawable.main_first_icon_selector, R.drawable.main_second_icon_selector,
            R.drawable.main_third_icon_selector, R.drawable.main_fourth_icon_selector
    };
    private static final int[] TAB_BOTTOM_NAME_ARRAY = {
            R.string.host_first_tab, R.string.host_second_tab,
            R.string.host_third_tab, R.string.host_fourth_tab
    };
    private final Class<? extends BaseFragment>[] FRAGMENTS = new Class[]{
            MainFirstFragment.class, MainSecondFragment.class, MainThirdFragment.class, MainFourthFragment.class
    };

    private ShowFragmentHelper mFragHelper;
    private ActivityMainBinding mViewBinding;
    private long mLastBackPressTime = 0;
    private static final int BACK_EXIT_INTERVAL = 3000;
//    private ViewPager2SlowScrollHelper slowScrollHelper;

//    private static final int AUTO_SCROLL_DELAY = 3000; // 自动滚动延迟时间，单位毫秒
//    private final Handler handler = new Handler(Looper.getMainLooper());
//    private final Runnable autoScrollRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (isAutoScrolling) {
//                int currentItem = bannerViewPager.getCurrentItem();

    /// /                bannerViewPager.setCurrentItem(currentItem + 1);
//                slowScrollHelper.setCurrentItem(currentItem + 1);
//                handler.postDelayed(this, AUTO_SCROLL_DELAY);
//            }
//        }
//    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentLayout(mViewBinding.getRoot());
        getTitleHelper().hideTitleBar();

        mFragHelper = new ShowFragmentHelper(getSupportFragmentManager(), FRAGMENTS);
        initBottomTabs();
        showTab(TAB_FIRST);
        setupBackPressHandler();


        List<String> imageList = Arrays.asList(
                "https://qd.shouji.qihucdn.com/media/7596e61dd2bc80488dbca79665ec1252/660127d7974f7.png",
                "https://d02.qd.shouji.360tpcdn.com/media/3768e5340f2139e71661b805718e4cce/67d3e3a7d7717.png",
                "https://qd.shouji.qihucdn.com/media/80d15cfc4174f0bb48e9231400160487/6602aa5c7dfde.png",
                "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png",
                "https://qd.shouji.qihucdn.com/media/3471cdbe7ce5812df964fbd68226edc0/6602aa4ad6b7f.png"
        );


        AutoGalleryBannerView2 autoGalleryBannerView2 = new AutoGalleryBannerView2(mViewBinding.bannerViewPager);
        autoGalleryBannerView2.setDataList(imageList);
        autoGalleryBannerView2.setItemMargin(-10);

//        bannerViewPager = findViewById(R.id.bannerViewPager);
//        // 创建ViewPager2SlowScrollHelper实例，设置滚动时长
//        slowScrollHelper = new ViewPager2SlowScrollHelper(bannerViewPager, 500);
//
//        List<String> imageList = Arrays.asList(
//                "https://qd.shouji.qihucdn.com/media/7596e61dd2bc80488dbca79665ec1252/660127d7974f7.png",
//                "https://d02.qd.shouji.360tpcdn.com/media/3768e5340f2139e71661b805718e4cce/67d3e3a7d7717.png",
//                "https://qd.shouji.qihucdn.com/media/80d15cfc4174f0bb48e9231400160487/6602aa5c7dfde.png",
//                "https://qd.shouji.qihucdn.com/media/fa4c53b380a75882404d303a2d4326b9/6602aa7e16e34.png",
//                "https://qd.shouji.qihucdn.com/media/3471cdbe7ce5812df964fbd68226edc0/6602aa4ad6b7f.png"
//        );
//
//        BannerAdapter adapter = new BannerAdapter(imageList);
//        bannerViewPager.setAdapter(adapter);
//        bannerViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
//
//        // 设置左右两侧都可以预览
//        bannerViewPager.setOffscreenPageLimit(3);
//        RecyclerView recyclerView = (RecyclerView) bannerViewPager.getChildAt(0);
//        recyclerView.setClipToPadding(false);
//        recyclerView.setClipChildren(false);


//        bannerViewPager.setPageTransformer(new GalleryPageTransformer());
//
//
//
//
//
//
//
//        // 添加 item 间距（可选）
//        bannerViewPager.addItemDecoration(new RecyclerView.ItemDecoration() {
//            @Override
//            public void getItemOffsets(@NonNull Rect outRect,
//                                       @NonNull View view,
//                                       @NonNull RecyclerView parent,
//                                       @NonNull RecyclerView.State state) {
//                int position = parent.getChildAdapterPosition(view);
//                int itemCount = state.getItemCount();
//                if (position < itemCount - 1) {
//                    outRect.right =  0;
//                }
//            }
//        });
//
//        // 设置初始位置为中间位置
//        int initialPosition = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2 % imageList.size());
//        bannerViewPager.setCurrentItem(initialPosition,false);
//
//        // 设置监听器，实现无缝循环
//        bannerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
////            @Override
////            public void onPageSelected(int position) {
////                if (position == 0) {
////                    int newPosition = Integer.MAX_VALUE - imageList.size();
////                    bannerViewPager.setCurrentItem(newPosition, false);
////                } else if (position == Integer.MAX_VALUE - 1) {
////                    bannerViewPager.setCurrentItem(imageList.size(), false);
////                }
////            }
//
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
//                    // 用户开始拖动，停止自动滚动
//                    isAutoScrolling = false;
//                    handler.removeCallbacks(autoScrollRunnable);
//                } else if (state == ViewPager2.SCROLL_STATE_IDLE && !isAutoScrolling) {
//                    // 用户停止拖动，恢复自动滚动
//                    isAutoScrolling = true;
//                    handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
//                }
//            }
//        });
//
//        // 开始自动滚动
//        if (isAutoScrolling) {
//            handler.postDelayed(autoScrollRunnable, AUTO_SCROLL_DELAY);
//        }

    }

    private void initBottomTabs() {
        for (int index = 0; index < TAB_BOTTOM_ID_ARRAY.length; index++) {
            View tabView = mViewBinding.bottomTab.findViewById(TAB_BOTTOM_ID_ARRAY[index]);
            tabView.setOnClickListener(this);

            ImageView icon = tabView.findViewById(R.id.image_view);
            TextView label = tabView.findViewById(R.id.text_view);

            icon.setImageResource(TAB_BOTTOM_ICON_ARRAY[index]);
            label.setText(TAB_BOTTOM_NAME_ARRAY[index]);
        }
    }

    public void showTab(int selectedIndex) {
        if (selectedIndex < 0 || selectedIndex >= FRAGMENTS.length) return;

        mFragHelper.showTabFragment(R.id.fragment_base_id, selectedIndex);

        for (int i = 0; i < TAB_BOTTOM_ID_ARRAY.length; i++) {
            View tabView = mViewBinding.bottomTab.findViewById(TAB_BOTTOM_ID_ARRAY[i]);
            tabView.setSelected(i == selectedIndex);
        }
    }

    public int getSelectedTab() {
        return mFragHelper.getSelectedTab();
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                long currentTime = System.currentTimeMillis();
                if (currentTime - mLastBackPressTime < BACK_EXIT_INTERVAL) {
                    finish();
                } else {
                    ToastUtils.show(getString(R.string.quit_alert));
                    mLastBackPressTime = currentTime;
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < TAB_BOTTOM_ID_ARRAY.length; i++) {
            if (v.getId() == TAB_BOTTOM_ID_ARRAY[i]) {
                showTab(i);
                return;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        handler.removeCallbacks(autoScrollRunnable);
    }

    @Override
    public void onBroadcastReceiver(String myAction, Bundle bundle) {
        super.onBroadcastReceiver(myAction, bundle);
    }

}
