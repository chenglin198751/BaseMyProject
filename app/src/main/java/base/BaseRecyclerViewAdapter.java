package base;

import android.support.v7.widget.RecyclerView;

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

    public void add(T t) {
        if (null != t) {
            list.add(t);
            notifyDataSetChanged();
        }
    }

    public void add(int pos, T t) {
        if (null != t && pos >= 0 && pos <= list.size() - 1) {
            list.add(pos, t);

        } else if (null != t && pos == list.size()) {
            list.add(t);
        }
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        if (list != null && pos < list.size()) {
            list.remove(pos);
            notifyDataSetChanged();
        }
    }

    public void remove(T t) {
        if (null != t) {
            list.remove(t);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        list.clear();
        notifyDataSetChanged();
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
