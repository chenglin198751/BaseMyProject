package com.wcl.test.base;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * weichenglin create in 15/9/17
 */
public abstract class BaseRecyclerViewAdapter<T> extends RecyclerView.Adapter<BaseRecyclerViewHolder> {
    protected List<T> list = new ArrayList<T>();

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
    }

    private int getItemPosition(T t) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equals(t)) {
                return i;
            }
        }
        return 0;
    }

    public void appendDataList(Collection<? extends T> collection) {
        if (null != collection) {
            list.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public List<T> getData() {
        return list;
    }

    public void setDataList(Collection<? extends T> collection) {
        if (null != collection) {
            list.clear();
            list.addAll(collection);
            notifyDataSetChanged();
        }
    }
}
