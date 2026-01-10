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

**7、基础语法** ：

    1、Java中的与或运算符 |和&，kotlin中使用or和and关键字来替代
    2、关于companion object使用：
        2-1、const只能在companion中被使用。
        2-2、const val 修饰的属性相当于java中的public static final修饰的常量，可以通过类名直接访问(反编译会发现变成了public static final)。
        2-3、val 修饰的属性相当于java中private static final修饰的常量，由于可见行为private，所以只能通过生成getter方法访问(反编译会发现变成了private static final)。
    3、Int.inc()自增,Int.dec()自减，但都不会改变原变量的值

**8、单例实现** ：

    object UserManager {
        fun getUser(){
        }
    }

**9、关键词** ：

    1、apply：它允许你在对象上执行一系列初始化操作，并且返回这个对象本身
        val textView = TextView(context).apply {
            text = "Hello, Kotlin!"
            setTextColor(Color.BLACK)
            textSize = 16f
        }

    2、takeIf：用于根据给定的条件判断是否 “获取” 调用它的对象。如果条件满足，它返回调用对象本身；如果条件不满足，则返回 null。
        val number = 10
        val result = number.takeIf { it > 5 }
        println(result) // 输出: 10

        // 结合空合并操作符 ?: 使用
        val str: String? = null
        val displayStr = str.takeIf { it.isNotEmpty() }?: "default"
        println(displayStr) // 输出:default

**10、声明静态方法** ：

    @JvmStatic
    fun startLocalPicSelectAty()