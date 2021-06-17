package cheerly.mybaseproject.main

import android.os.Bundle
import android.view.View
import cheerly.mybaseproject.R
import cheerly.mybaseproject.base.BaseFragment

/**
 * Created by chenglin on 2017-9-14.
 */
class MainThirdFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getContentLayout(): Int {
        return R.layout.main_third_frag_layout
    }

    public override fun onViewCreated(savedInstanceState: Bundle?, view: View) {}
}