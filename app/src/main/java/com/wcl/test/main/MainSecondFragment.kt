package com.wcl.test.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.wcl.test.base.BaseFragment
import com.wcl.test.databinding.MainSecondFragLayoutBinding
import com.wcl.test.test.TestUserViewModel

/**
 * Created by chenglin on 2017-9-14.
 */
class MainSecondFragment : BaseFragment() {
    private var _binding: MainSecondFragLayoutBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: TestUserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[TestUserViewModel::class.java]
    }

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
        binding.lifecycleOwner = viewLifecycleOwner
        binding.testViewModel = viewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}