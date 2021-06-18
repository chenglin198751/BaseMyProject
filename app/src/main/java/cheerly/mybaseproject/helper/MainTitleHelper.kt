package cheerly.mybaseproject.helper

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import cheerly.mybaseproject.R
import cheerly.mybaseproject.base.BaseActivity

/**
 * Created by chenglin on 2017-5-23.
 */
class MainTitleHelper(private val mBaseActivity: BaseActivity) {
    private val mTitleView: View
    private val mMenuView: RelativeLayout
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

    /**
     * 为右侧的菜单增加指定的view
     */
    fun addMenuView(view: View?) {
        mMenuView.visibility = View.VISIBLE
        mMenuView.removeAllViews()
        mMenuView.addView(view, RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    }

    /**
     * 为右侧的第一个菜单图片设置点击监听
     */
    fun addMenuIcon(resId: Int, listener: View.OnClickListener?) {
        val menuTv = mTitleView.findViewById<TextView>(R.id.menu_text)
        val menuIcon = mTitleView.findViewById<ImageView>(R.id.menu_icon)
        mMenuView.visibility = View.VISIBLE
        menuTv.visibility = View.GONE
        menuIcon.visibility = View.VISIBLE
        menuIcon.setImageResource(resId)
        menuIcon.setOnClickListener(listener)
    }

    /**
     * 为右侧的第一个菜单文字设置点击监听
     */
    fun addMenuText(text: String?, listener: View.OnClickListener?) {
        val menuTv = mTitleView.findViewById<TextView>(R.id.menu_text)
        val menuIcon = mTitleView.findViewById<ImageView>(R.id.menu_icon)
        mMenuView.visibility = View.VISIBLE
        menuTv.visibility = View.VISIBLE
        menuIcon.visibility = View.GONE
        menuTv.text = text
        menuTv.setOnClickListener(listener)
    }

    /**
     * 设置右侧的菜单隐藏还是显示
     */
    fun setMenuVisible(visible: Int) {
        mMenuView.visibility = visible
    }

    init {
        mTitleView = mBaseActivity.findViewById(R.id.main_title)
        mMenuView = mTitleView.findViewById(R.id.menu_linear)
        mTitleTv = mTitleView.findViewById(R.id.title_text)
        mTitleView.findViewById<View>(R.id.back_btn).setOnClickListener { mBaseActivity.finish() }
    }
}