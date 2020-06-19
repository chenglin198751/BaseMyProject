package cheerly.mybaseproject.test;

import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cheerly.mybaseproject.R;
import cheerly.mybaseproject.base.BaseActivity;

public class TestActivity extends BaseActivity {
    private RecyclerView mRecyclerView1;
    private RecyclerView mRecyclerView3;
    private TestRecyclerAdapter adapter1;
    private TestRecyclerAdapter adapter3;
    private List<String> mLists = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.test_activity_layout);

        mRecyclerView1 = findViewById(R.id.recyclerView1);
        mRecyclerView3 = findViewById(R.id.recyclerView3);

        adapter1 = new TestRecyclerAdapter(getContext());
        adapter3 = new TestRecyclerAdapter(getContext());

        for (int i = 0; i < 15; i++) {
            mLists.add(i + "");
        }

        adapter1.setDataList(mLists);
        adapter3.setDataList(mLists);

        mRecyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView3.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView1.setAdapter(adapter1);
        mRecyclerView3.setAdapter(adapter3);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
