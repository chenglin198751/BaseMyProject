package com.wcl.test.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import com.wcl.test.R
import com.wcl.test.base.BaseActivity
import com.wcl.test.databinding.ActivityMainBinding
import com.wcl.test.helper.DialPhoneBroadcastReceiver
import com.wcl.test.helper.ShowFragmentHelper
import com.wcl.test.widget.ToastUtils

class MainActivity : BaseActivity(), View.OnClickListener {
    private val TIME_LONG = 3 * 1000
    private var mLastTime: Long = 0
    private val FRAGMENTS = arrayOf<Class<*>>(
        MainFirstFragment::class.java,
        MainSecondFragment::class.java,
        MainThirdFragment::class.java,
        MainFourthFragment::class.java
    )
    private var mFragHelper: ShowFragmentHelper? = null
    private var mMainFirstFragment: MainFirstFragment? = null
    private var mMainSecondFragment: MainSecondFragment? = null
    private var mMainThirdFragment: MainThirdFragment? = null
    private var mMainFourthFragment: MainFourthFragment? = null

    private var binding: ActivityMainBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //由于Manifest的style v27中设置了使用缺口屏，所以这里恢复为默认不使用缺口屏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val layoutParams = window.attributes
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            window.attributes = layoutParams
        }
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentLayout(binding?.root)
        titleHelper.hideTitleBar()
        mFragHelper = ShowFragmentHelper(supportFragmentManager, FRAGMENTS)
        initView()
        showTab(TAB_FIRST)
    }

    private fun initView() {
        for (index in TAB_BOTTOM_ID_ARRAY.indices) {
            findViewById<View>(TAB_BOTTOM_ID_ARRAY[index]).setOnClickListener(this)
            val imageView =
                findViewById<View>(TAB_BOTTOM_ID_ARRAY[index]).findViewById<View>(R.id.image_view) as ImageView
            val textView =
                findViewById<View>(TAB_BOTTOM_ID_ARRAY[index]).findViewById<View>(R.id.text_view) as TextView
            imageView.setImageResource(TAB_BOTTOM_ICON_ARRAY[index])
            textView.setText(TAB_BOTTOM_NAME_ARRAY[index])
        }
    }

    /**
     * 用来让某个tab显示
     */
    private fun showTab(selectedIndex: Int) {
        val baseFragment = mFragHelper!!.showTabFragment(R.id.fragment_base_id, selectedIndex)
        for (index in TAB_BOTTOM_ID_ARRAY.indices) {
            val isSelected = (selectedIndex == index);
            findViewById<View>(TAB_BOTTOM_ID_ARRAY[index]).isSelected = isSelected
        }

        when (selectedIndex) {
            TAB_FIRST -> mMainFirstFragment = baseFragment as MainFirstFragment
            TAB_SECOND -> mMainSecondFragment = baseFragment as MainSecondFragment
            TAB_THIRD -> mMainThirdFragment = baseFragment as MainThirdFragment
            TAB_FOURTH -> mMainFourthFragment = baseFragment as MainFourthFragment
        }
    }

    val selectedTab: Int
        get() = mFragHelper?.mSelectedTab ?: 0

    //解决fragment回收后重建空指针的问题
    @SuppressLint("MissingSuperCall")
    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState);

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBackPressed() {
        val exitTime = System.currentTimeMillis()
        if (exitTime - mLastTime < TIME_LONG) {
            super.onBackPressed()
        } else {
            ToastUtils.show(getString(R.string.quit_alert))
            mLastTime = exitTime
        }
    }

    override fun onBroadcastReceiver(action: String, bundle: Bundle) {
        super.onBroadcastReceiver(action, bundle)
        if (DialPhoneBroadcastReceiver.SECRET_CODE == action) {
            DialPhoneBroadcastReceiver.showDebugView(this)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.tab_first -> showTab(TAB_FIRST)
            R.id.tab_second -> showTab(TAB_SECOND)
            R.id.tab_third -> showTab(TAB_THIRD)
            R.id.tab_fourth -> showTab(TAB_FOURTH)
        }
    }

    companion object {
        const val CLASS_NAME = "MainActivity"
        const val TAB_FIRST = 0
        const val TAB_SECOND = 1
        const val TAB_THIRD = 2
        const val TAB_FOURTH = 3
        private val TAB_BOTTOM_ID_ARRAY =
            intArrayOf(R.id.tab_first, R.id.tab_second, R.id.tab_third, R.id.tab_fourth)
        private val TAB_BOTTOM_ICON_ARRAY = intArrayOf(
            R.drawable.main_first_icon_selector, R.drawable.main_second_icon_selector,
            R.drawable.main_third_icon_selector, R.drawable.main_fourth_icon_selector
        )
        private val TAB_BOTTOM_NAME_ARRAY = intArrayOf(
            R.string.host_first_tab, R.string.host_second_tab,
            R.string.host_third_tab, R.string.host_fourth_tab
        )
    }
}