package com.wcl.test.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.LayoutRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public abstract class BaseListViewAdapter<T, VH extends BaseListViewAdapter.ViewHolder> extends BaseAdapter {
    protected final List<T> list = new ArrayList<>();
    protected final LayoutInflater inflater;
    private final int layoutId;

    public BaseListViewAdapter(@NonNull Context context, @LayoutRes int layoutId) {
        this.inflater = LayoutInflater.from(context);
        this.layoutId = layoutId;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    @Nullable
    public T getItem(int position) {
        if (position < 0 || position >= list.size()) return null;
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        VH holder;
        if (convertView == null) {
            convertView = inflater.inflate(layoutId, parent, false);
            holder = createViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            //noinspection unchecked
            holder = (VH) convertView.getTag();
        }

        T item = getItem(position);
        if (item != null) {
            bindViewHolder(holder, item, position);
        }
        return convertView;
    }

    /**
     * 子类必须实现：如何创建 ViewHolder
     */
    @NonNull
    protected abstract VH createViewHolder(@NonNull View itemView);

    /**
     * 子类必须实现：如何绑定数据
     */
    protected abstract void bindViewHolder(@NonNull VH holder, @NonNull T item, int position);

    @MainThread
    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    @MainThread
    public void appendDataList(@NonNull Collection<? extends T> collection) {
        if (!collection.isEmpty()) {
            list.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public List<T> getData() {
        return Collections.unmodifiableList(list);
    }

    @MainThread
    public void setDataList(@Nullable Collection<? extends T> collection) {
        list.clear();
        if (collection != null && !collection.isEmpty()) {
            list.addAll(collection);
        }
        notifyDataSetChanged();
    }

    // ===== 内部 ViewHolder 类 =====
    public static abstract class ViewHolder {
        protected final View itemView;

        public ViewHolder(@NonNull View itemView) {
            this.itemView = itemView;
            initViews(itemView);
        }

        // 子类负责 findViewById
        protected abstract void initViews(@NonNull View itemView);
    }
}
