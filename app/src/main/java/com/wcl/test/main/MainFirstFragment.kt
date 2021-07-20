package com.wcl.test.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.wcl.test.base.BaseFragment
import com.wcl.test.databinding.MainFirstFragLayoutBinding
import com.wcl.test.test.TestSelectedPhotoActivity


/**
 * Created by chenglin on 2017-9-14.
 */
class MainFirstFragment : BaseFragment() {
    private lateinit var vBinding: MainFirstFragLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vBinding = MainFirstFragLayoutBinding.inflate(layoutInflater)
    }

    override fun onViewCreated(savedInstanceState: Bundle?, view: View) {
        vBinding.button1.setOnClickListener {
            val intent = Intent(context, TestSelectedPhotoActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBroadcastReceiver(action: String, bundle: Bundle) {
        super.onBroadcastReceiver(action, bundle)
    }

    override fun getContentLayout(): Int {
        return -1
    }

    override fun getContentView(): View {
        return vBinding.root
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}