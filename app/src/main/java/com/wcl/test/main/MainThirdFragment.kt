package com.wcl.test.main

import android.os.Bundle
import android.view.View
import com.wcl.test.base.BaseFragment
import com.wcl.test.base.viewBinding
import com.wcl.test.databinding.MainThirdFragLayoutBinding

/**
 * Created by chenglin on 2017-9-14.
 */
class MainThirdFragment : BaseFragment() {
    private val binding by viewBinding(MainThirdFragLayoutBinding::inflate)

    override fun getContentLayout(): Int {
        return 0
    }

    override fun getContentView(): View {
        return binding.root
    }

    override fun onViewCreated(savedInstanceState: Bundle?, view: View) {
        // 直接使用 binding 即可
    }
}