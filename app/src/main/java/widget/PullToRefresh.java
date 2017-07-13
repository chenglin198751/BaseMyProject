package widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import in.srain.cube.views.ptr.PtrDefaultHandler;
import in.srain.cube.views.ptr.PtrDefaultHandler2;
import in.srain.cube.views.ptr.PtrFrameLayout;
import in.srain.cube.views.ptr.PtrHandler2;
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

    public void setListener(final onListener listener) {
        setPtrHandler(new PtrHandler2() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return PtrDefaultHandler.checkContentCanBePulledDown(frame, content, header);
            }

            @Override
            public boolean checkCanDoLoadMore(PtrFrameLayout frame, View content, View footer) {
                return PtrDefaultHandler2.checkContentCanBePulledUp(frame, content, footer);
            }

            @Override
            public void onLoadMoreBegin(PtrFrameLayout frame) {
                if (listener != null) {
                    listener.onLoadMore();
                }
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                if (listener != null) {
                    listener.onRefresh();
                }
            }
        });
    }

    private void init() {
        //阻尼系数 --默认: 1.7f，越大，感觉下拉时越吃力。
        setResistance(1.5f);

        //触发刷新时移动的位置比例 --默认，1.2f，移动达到头部高度1.2倍时可触发刷新操作。
        setRatioOfHeaderHeightToRefresh(1.1f);

        //回弹延时 --默认 200ms，回弹到刷新高度所用时间
        setDurationToClose(200);

        //头部回弹时间 --默认1000ms--千万不能设置的超过500，否则会出现立刻下拉不刷新的bug
        setDurationToCloseHeader(500);

        //下拉刷新-- true为距离触发刷新；false为释放刷新
        setPullToRefresh(false);

        //刷新是保持头部 --默认值 true
        setKeepHeaderWhenRefresh(true);

        //初始化头部、尾部
        addHeaderFooter();
    }

    private void addHeaderFooter() {
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

    public interface onListener {
        void onRefresh();

        void onLoadMore();
    }
}
