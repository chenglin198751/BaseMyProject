package cheerly.mybaseproject.view.pullrefresh;

import android.content.Context;
import android.util.AttributeSet;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;


/**
 * 地址：https://github.com/scwang90/SmartRefreshLayout
 * Created by chenglin on 2017-7-12.
 * 支持下拉刷新和上拉加载更多
 */

public class PullToRefreshView extends SmartRefreshLayout {

    public PullToRefreshView(Context context) {
        super(context);
        init();
    }

    public PullToRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setListener(final onListener listener) {
        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                if (listener != null) {
                    listener.onRefresh();
                }
            }
        });
        setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(RefreshLayout refreshlayout) {
                if (listener != null) {
                    listener.onLoadMore();
                }
            }
        });
    }


    /**
     * https://github.com/scwang90/SmartRefreshLayout/blob/master/art/md_property.md
     */
    private void init() {
        setDragRate(0.6f);//显示下拉高度/手指真实下拉高度=阻尼效果（摩擦系数）
        setEnableOverScrollBounce(false);//是否启用越界回弹，就是回到顶部时回弹一下

        //初始化头部、尾部
        addHeaderFooter();
    }

    private void addHeaderFooter() {
        CustomRefreshHeader header = new CustomRefreshHeader(getContext());
        setRefreshHeader(header);

        CustomRefreshFooter footer = new CustomRefreshFooter(getContext());
        setRefreshFooter(footer);
    }

    @Override
    public PullToRefreshView finishRefresh() {
        finishRefresh(0);
        return this;
    }

    @Override
    public PullToRefreshView finishLoadMore() {
        finishLoadMore(0);
        return this;
    }

    @Override
    public boolean autoRefresh() {
        return super.autoRefresh();
    }

    @Override
    public boolean autoRefresh(int delayed) {
        return super.autoRefresh(delayed);
    }

    @Override
    public PullToRefreshView finishLoadMoreWithNoMoreData() {
        super.finishLoadMoreWithNoMoreData();
        return this;
    }


    public interface onListener {
        void onRefresh();

        void onLoadMore();
    }
}
