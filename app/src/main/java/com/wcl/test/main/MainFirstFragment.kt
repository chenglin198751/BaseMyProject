package com.wcl.test.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wcl.test.R
import com.wcl.test.base.BaseFragment
import com.wcl.test.test.TestViewPager2Activity
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
            val intent = Intent(context, TestViewPager2Activity::class.java)
            startActivity(intent)
        }
    }

    override fun onBroadcastReceiver(action: String, bundle: Bundle) {
        super.onBroadcastReceiver(action, bundle)
    }

    override fun getContentLayout(): Int {
        return R.layout.main_first_frag_layout
    }


}