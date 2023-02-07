# ConstraintLayout学习：

**1、ConstraintLayout使用说明：** https://mp.weixin.qq.com/s/Z_TnoyMRYZEQXvlqiKX8Uw

**2、当设置一个view和另一个view上下对齐时，如果不指定强制约束宽高，那么宽高都会自适应；如果指定了约束宽高，那么宽高会和上顶部和下顶部对齐：**

    约束宽高代码：
    app:layout_constrainedWidth="true"
    app:layout_constrainedHeight="true"