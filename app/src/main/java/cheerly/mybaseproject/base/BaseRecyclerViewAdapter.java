package cheerly.mybaseproject.base;

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

    public void add(T t) {
        if (null != t) {
            list.add(t);
            notifyItemChanged(getItemPosition(t));
        }
    }

    public void add(int pos, T t) {
        if (null != t && pos >= 0 && pos <= list.size() - 1) {
            list.add(pos, t);

        } else if (null != t && pos == list.size()) {
            list.add(t);
        }
        notifyItemChanged(pos);
    }

    public void remove(int pos) {
        if (list != null && pos < list.size()) {
            list.remove(pos);
            notifyItemRemoved(pos);
        }
    }

    public void remove(T t) {
        if (null != t) {
            notifyItemRemoved(getItemPosition(t));
        }
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
