package cheerly.mybaseproject;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.base.BaseRecyclerViewAdapter;
import cheerly.mybaseproject.base.BaseRecyclerViewHolder;
import cheerly.mybaseproject.utils.Constants;
import cheerly.mybaseproject.utils.SmartImageLoader;


/**
 * 用法：
 * BottomDialogFragment dialog = BottomDialogFragment.newInstance();
 * dialog.show(getSupportFragmentManager(), "");
 */
public class BottomDialogFragment extends BottomSheetDialogFragment {
    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;


    public static BottomDialogFragment newInstance() {
        final BottomDialogFragment fragment = new BottomDialogFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_listview, container, false);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Constants.getScreenHeight() / 3 * 2));
        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new MyAdapter(getContext());
        mRecyclerView = view.findViewById(R.id.list_view);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        setData(20, true);
    }

    private boolean setData(int count, boolean isRefresh) {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add("" + (mAdapter.getItemCount() + i));
        }

        if (isRefresh) {
            mAdapter.setDataList(list);
        } else {
            mAdapter.appendDataList(list);
        }

        if (mAdapter.getItemCount() > 20) {
            return false;
        }

        return true;
    }


    public static class MyAdapter extends BaseRecyclerViewAdapter<String> {

        private Context mContext;

        public MyAdapter(Context context) {
            this.mContext = context;
        }


        @Override
        public ListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.test_item_2, parent, false);
            return new ListHolder(view);
        }

        @Override
        public void onBindViewHolder(BaseRecyclerViewHolder holder, int position) {
            if (holder instanceof ListHolder) {
                ListHolder listHolder = (ListHolder) holder;
                listHolder.onBind(position);
            }

        }

        private static class ListHolder extends BaseRecyclerViewHolder {
            private String url = "http://5b0988e595225.cdn.sohucs.com/images/20170922/c7e95cf930a64a27b616e8c77525645b.jpeg";
            ImageView imageView;

            public ListHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.image_view);

            }

            @Override
            public void onBind(int position) {
                SmartImageLoader.getInstance().load(imageView, url, -1, -1);
            }

        }
    }

}