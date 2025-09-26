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
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    protected final List<T> list = new ArrayList<>();

    @Override
    public int getItemCount() {
        return list.size();
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
        return new ArrayList<>(list);
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
