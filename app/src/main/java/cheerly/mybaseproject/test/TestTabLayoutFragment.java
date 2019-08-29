package cheerly.mybaseproject.test;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cheerly.mybaseproject.base.BaseFragment;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-9-14.
 */

public class TestTabLayoutFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    protected int getContentLayout() {
        return R.layout.test_fragment_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {
        TextView content = (TextView) getView().findViewById(R.id.content);
        content.setText(getArguments().getString("index"));
    }
}
