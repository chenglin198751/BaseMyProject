package com.wcl.test.main

import android.os.Bundle
import android.view.View
import com.wcl.test.R
import com.wcl.test.base.BaseFragment

/**
 * Created by chenglin on 2017-9-14.
 */
class MainFourthFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getContentLayout(): Int {
        return R.layout.main_fourth_frag_layout
    }

    public override fun onViewCreated(savedInstanceState: Bundle?, view: View?) {}
}