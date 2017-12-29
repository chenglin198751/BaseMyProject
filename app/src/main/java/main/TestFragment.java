package main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import base.BaseFragment;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getContentLayout() {
        return R.layout.fragment_test_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        TextView content = (TextView) getView().findViewById(R.id.content);
        content.setText(getArguments().getString("index"));
    }
}
