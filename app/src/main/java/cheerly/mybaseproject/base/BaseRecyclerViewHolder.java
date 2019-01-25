package cheerly.mybaseproject.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by chenglin on 2017-9-22.
 */

public abstract class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {
    public BaseRecyclerViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void onBind(int position);
}
