package com.wcl.test.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * weichenglin create in 15/9/17
 */
public abstract class BaseListViewAdapter<T> extends BaseAdapter {
    protected final List<T> list = new ArrayList<>();
    protected final LayoutInflater inflater;

    public BaseListViewAdapter(@NonNull Context context) {
        super();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    @Nullable
    public T getItem(int position) {
        if (position < 0 || position >= list.size()) {
            return null;
        }
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    @NonNull
    public abstract View getView(int position, View convertView, ViewGroup parent);

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
}
