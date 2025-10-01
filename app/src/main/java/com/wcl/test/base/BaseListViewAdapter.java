package com.wcl.test.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用的 ListView Adapter，支持多布局
 */
public abstract class BaseListViewAdapter<T, VH extends BaseListViewAdapter.BaseListViewHolder<T>> extends android.widget.BaseAdapter {
    protected final Context mContext;
    private final List<T> mData = new ArrayList<>();

    public BaseListViewAdapter(Context context) {
        this.mContext = context;
    }

    public void setDataList(List<T> list) {
        mData.clear();
        if (list != null) {
            mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void appendDataList(List<T> list) {
        if (list != null) {
            mData.addAll(list);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return mData;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 默认单布局，子类可重写
    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    // 默认一个布局，子类可重写
    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @NonNull
    protected abstract VH createViewHolder(@NonNull ViewGroup parent, int viewType);

    protected abstract void bindViewHolder(@NonNull VH holder, @NonNull T item, int position);

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int viewType = getItemViewType(position);
        VH holder;
        if (convertView == null) {
            holder = createViewHolder(parent, viewType);
            convertView = holder.itemView;
            convertView.setTag(holder);
        } else {
            //noinspection unchecked
            holder = (VH) convertView.getTag();
        }

        bindViewHolder(holder, getItem(position), position);
        return convertView;
    }

    public abstract static class BaseListViewHolder<T> {
        protected final View itemView;

        public BaseListViewHolder(@NonNull View itemView) {
            this.itemView = itemView;
            bindViews(itemView);
        }

        /**
         * 初始化控件
         */
        protected abstract void bindViews(@NonNull View itemView);

        /**
         * 绑定数据，子类必须实现
         */
        public abstract void onBind(@NonNull T item, int position);
    }
}
