package com.wcl.test.base;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * weichenglin create in 15/9/17
 */
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder<T>> {
    private final List<T> list = new ArrayList<>();

    @Override
    public int getItemCount() {
        return list.size();
    }

    protected T getItem(int position) {
        return list.get(position);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseRecyclerViewHolder<T> holder, int position) {
        holder.onBind(getItem(position), position);
    }

    @MainThread
    public void clear() {
        int size = list.size();
        list.clear();
        if (size > 0) {
            notifyItemRangeRemoved(0, size);
        }
    }

    public int getItemPosition(@NonNull T t) {
        for (int i = 0; i < list.size(); i++) {
            if (t.equals(list.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @MainThread
    public void appendDataList(Collection<? extends T> collection) {
        if (collection != null && !collection.isEmpty()) {
            int start = list.size();
            list.addAll(collection);
            notifyItemRangeInserted(start, collection.size());
        }
    }

    public List<T> getData() {
        return list;
    }

    @MainThread
    public void add(T item) {
        int position = list.size();
        list.add(item);
        notifyItemInserted(position);
    }

    @MainThread
    public void setDataList(Collection<? extends T> collection) {
        list.clear();
        if (collection != null && !collection.isEmpty()) {
            list.addAll(collection);
        }
        notifyDataSetChanged();
    }

}
