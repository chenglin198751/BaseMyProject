package cheerly.mybaseproject.main

import android.os.Bundle
import android.view.View
import cheerly.mybaseproject.R
import cheerly.mybaseproject.base.BaseFragment
import kotlinx.android.synthetic.main.main_first_frag_layout.*


/**
 * Created by chenglin on 2017-9-14.
 */
class MainFirstFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(savedInstanceState: Bundle?, view: View) {
        button_1.setOnClickListener {

        }
    }

    override fun onBroadcastReceiver(action: String, bundle: Bundle) {
        super.onBroadcastReceiver(action, bundle)
    }

    override fun getContentLayout(): Int {
        return R.layout.main_first_frag_layout
    }


}