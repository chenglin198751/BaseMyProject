package com.wcl.test.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.wcl.test.base.BaseFragment
import com.wcl.test.databinding.MainSecondFragLayoutBinding

/**
 * Created by chenglin on 2017-9-14.
 */
class MainSecondFragment : BaseFragment() {
    private var _binding: MainSecondFragLayoutBinding? = null
    private val binding get() = _binding!!

    override fun getContentLayout(): Int {
        return 0
    }

    override fun getContentView(): View? {
        if (_binding == null) {
            _binding = MainSecondFragLayoutBinding.inflate(LayoutInflater.from(context))
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