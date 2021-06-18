# kotlin语法规则
1、kotin语法中，方法里可以再写一个方法。把代码反编译后发现，原理就是用方法名生成了一个内部类，被嵌套的方法放在了内部类里。
2、kotin语法中，可以对任何类实现扩展函数。把代码反编译后发现，原理就是生成了一个public static方法。


**1、集合类创建** ：
     val numbers = listOf<Int>(1, 2, 3)
     val numbers = mutableListOf<Int>(1,2,6,3,7,9,4)

**2、字符连接** ：
val url = "wotbox://client/market?jumpType=detail&goodsId=${item.product_code}"

**3、判空操作** ：
val size = pkgList?.size?:0
mFragHelper!!.mSelectedTab(!!.符号表示，如果mFragHelper为空就不管，崩就崩)

**4、协程使用** ：
        val job = GlobalScope.launch(Dispatchers.Default) {
            Log.v("tag_99", "000 = " + BaseUtils.isUiThread())
            val pkgList = getPkgListNew()
            Log.v("tag_99", "111 = " + BaseUtils.isUiThread())
            withContext(Dispatchers.Main) {
                Log.v("tag_99", "222 = pkgList.size = " + (pkgList?.size?:0) + "  " + BaseUtils.isUiThread())
            }
        }
        job.start()

//绑定生命周期的协程：
        lifecycleScope.launch (Dispatchers.Default){
        }.start()

**5、java get set 简写** ：
    //---------java---------
    public void setTitle(String titleStr) {
        mTitleTv.setText(titleStr);
    }
    public String getTitle() {
        return mTitleTv.getText().toString();
    }

    //---------kotlin---------
    var title: String?
        get() = mTitleTv.text.toString()
        set(titleStr) {
            mTitleTv.text = titleStr
        }

**6、作用域** ：
    public：默认，总是可见
    internal：同模块可见
    private：声明范围与同模块的子作用域可见
    protected：类似于private，但对子类也可见