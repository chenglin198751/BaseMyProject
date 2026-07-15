package com.wcl.test.helper

import android.view.View
import android.widget.TextView
import com.wcl.test.R
import com.wcl.test.base.BaseActivity

/**
 * Activity 标题栏辅助类
 * Created by chenglin on 2017-5-23
 */
class MainTitleHelper(private val activity: BaseActivity) {

    private val titleView: View by lazy { activity.findViewById(R.id.main_title) }
    private val titleTextView: TextView by lazy { titleView.findViewById(R.id.title_text) }
    private val backBtn: View by lazy { titleView.findViewById(R.id.back_btn) }

    fun getTitleView(): View {
        return titleView
    }

    /**
     * 标题文字
     */
    var title: String?
        get() = titleTextView.text.toString()
        set(value) {
            titleTextView.text = value
        }

    /**
     * 设置标题文字资源 id
     */
    fun setTitle(titleResId: Int) {
        titleTextView.setText(titleResId)
    }

    /**
     * 隐藏标题栏
     */
    fun hideTitleBar() {
        titleView.visibility = View.GONE
    }

    /**
     * 显示标题栏
     */
    fun showTitleBar() {
        titleView.visibility = View.VISIBLE
    }

    /**
     * 设置返回按钮点击事件，默认 finish Activity
     */
    fun setReturnListener(listener: (() -> Unit)? = null) {
        backBtn.setOnClickListener {
            listener?.invoke() ?: activity.finish()
        }
    }

    init {
        // 默认返回按钮点击 finish
        setReturnListener()
    }
}
