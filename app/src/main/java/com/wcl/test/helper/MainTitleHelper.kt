package com.wcl.test.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.wcl.test.R
import com.wcl.test.base.BaseActivity

/**
 * Created by chenglin on 2017-5-23.
 */
class MainTitleHelper(private val mBaseActivity: BaseActivity) {
    private val mTitleView: View
    private val mTitleTv: TextView

    /**
     * 设置标题
     */
    public var title: String?
        get() = mTitleTv.text.toString()
        set(titleStr) {
            mTitleTv.text = titleStr
        }

    /**
     * 设置标题
     */
    fun setTitle(title_resId: Int) {
        mTitleTv.setText(title_resId)
    }

    /**
     * 隐藏标题栏
     */
    fun hideTitleBar() {
        mTitleView.visibility = View.GONE
    }

    /**
     * 设置左上角返回图片的点击监听事件
     */
    fun setReturnListener(listener: View.OnClickListener?) {
        mTitleView.findViewById<View>(R.id.back_btn).setOnClickListener(listener)
    }

    init {
        mTitleView = mBaseActivity.findViewById(R.id.main_title)
        mTitleTv = mTitleView.findViewById(R.id.title_text)
        mTitleView.findViewById<View>(R.id.back_btn).setOnClickListener { mBaseActivity.finish() }
    }
}