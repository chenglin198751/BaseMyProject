# kotlin语法规则

**1、集合类创建** ：
     val numbers = listOf<Int>(1, 2, 3)
     val numbers = mutableListOf<Int>(1,2,6,3,7,9,4)

**2、字符连接** ：
val url = "wotbox://client/market?jumpType=detail&goodsId=${item.product_code}"

**3、判空操作** ：
val size = pkgList?.size?:0

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
        
        