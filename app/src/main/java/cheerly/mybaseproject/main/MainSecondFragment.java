package cheerly.mybaseproject.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cheerly.mybaseproject.base.BaseFragment;
import cheerly.mybaseproject.R;

/**
 * Created by chenglin on 2017-9-14.
 */

public class MainSecondFragment extends BaseFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayout() {
        return R.layout.main_second_frag_layout;
    }

    @Override
    public void onViewCreated(Bundle savedInstanceState, View view) {

    }
}
