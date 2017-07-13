package widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import cheerly.mybaseproject.R;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.header.StoreHouseHeader;
import utils.MyUtils;

/**
 * 地址：https://github.com/captainbupt/android-Ultra-Pull-To-Refresh-With-Load-More
 * Created by chenglin on 2017-7-12.
 * 支持下拉刷新和上拉加载更多
 */

public class PullToRefresh extends PtrFrameLayout {
    public PullToRefresh(Context context) {
        super(context);
        init();
    }

    public PullToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefresh(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        //阻尼系数 --默认: 1.7f，越大，感觉下拉时越吃力。
        setResistance(1.5f);

        //触发刷新时移动的位置比例 --默认，1.2f，移动达到头部高度1.2倍时可触发刷新操作。
        setRatioOfHeaderHeightToRefresh(1.1f);

        //回弹延时 --默认 200ms，回弹到刷新高度所用时间
        setDurationToClose(200);

        //头部回弹时间 --默认1000ms
        setDurationToCloseHeader(800);

        //下拉刷新-- true为距离触发刷新；false为释放刷新
        setPullToRefresh(false);

        //刷新是保持头部 --默认值 true
        setKeepHeaderWhenRefresh(true);

        //初始化头部、尾部
        addHeaderFooter();
    }

    private void addHeaderFooter() {
//        StoreHouseHeader header = new StoreHouseHeader(getContext());
//        header.setPadding(0, (int) MyUtils.dip2px(20f), 0, (int) MyUtils.dip2px(20f));
//        header.initWithString("Pull Down Refresh");
//        header.setTextColor(Color.RED);
//        setHeaderView(header);
//        addPtrUIHandler(header);
//
//        StoreHouseHeader footer = new StoreHouseHeader(getContext());
//        footer.setPadding(0, (int) MyUtils.dip2px(20f), 0, (int) MyUtils.dip2px(20f));
//        footer.initWithString("Ultra Footer");
//        footer.setTextColor(Color.RED);
//        setFooterView(footer);
//        addPtrUIHandler(footer);

        CustomRefreshHeader header = new CustomRefreshHeader(getContext());
        setHeaderView(header.getView());
        addPtrUIHandler(header);

        StoreHouseHeader footer = new StoreHouseHeader(getContext());
        footer.setPadding(0, (int) MyUtils.dip2px(20f), 0, (int) MyUtils.dip2px(20f));
        footer.initWithString("Ultra Footer");
        footer.setTextColor(Color.RED);
        setFooterView(footer);
        addPtrUIHandler(footer);
    }
}
