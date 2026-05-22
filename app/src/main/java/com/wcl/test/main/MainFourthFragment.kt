package com.wcl.test.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.wcl.test.base.BaseFragment
import com.wcl.test.databinding.MainFourthFragLayoutBinding

/**
 * Created by chenglin on 2017-9-14.
 */
class MainFourthFragment : BaseFragment() {
    private var _binding: MainFourthFragLayoutBinding? = null
    private val binding get() = _binding!!

    override fun getContentLayout(): Int {
        return 0
    }

    override fun getContentView(): View? {
        if (_binding == null) {
            _binding = MainFourthFragLayoutBinding.inflate(LayoutInflater.from(context))
        }
        return _binding?.root
    }

    override fun onViewCreated(savedInstanceState: Bundle?, view: View) {
        // 使用 binding 进行 UI 操作
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}