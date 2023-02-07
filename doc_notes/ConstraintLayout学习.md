# ConstraintLayout学习：

**1、ConstraintLayout使用说明：** https://mp.weixin.qq.com/s/Z_TnoyMRYZEQXvlqiKX8Uw

**2、当设置一个view和另一个view上下对齐时，如果不指定强制约束宽高，那么宽高都会自适应；如果指定了约束宽高，那么宽高会和上顶部和下顶部对齐：**

        约束宽高代码：
        app:layout_constrainedWidth="true"
        app:layout_constrainedHeight="true"

**3、设置一个view相对另一个view居中，或者水平or竖直偏移：**

        若现在要实现水平偏移，给TextView1的layout_constraintHorizontal_bias赋一个范围为 0-1 的值，
        赋值为0，则TextView1在布局的最左侧；
        赋值为1，则TextView1在布局的最右侧；
        赋值为0.5，则水平居中；
        赋值为0.3，则更倾向于左侧。
        垂直偏移同理。

        app:layout_constraintHorizontal_bias="0.3"
        app:layout_constraintVertical_bias="0.3"
        app:layout_constraintEnd_toEndOf="@+id/view_bottom"
        app:layout_constraintStart_toStartOf="@+id/view_bottom"
        app:layout_constraintTop_toBottomOf="@+id/view_bottom"

**4、设置view的宽高比：**

        app:layout_constraintDimensionRatio="1:1"
        app:layout_constraintDimensionRatio="H,2:3"指的是 高:宽=2:3
        app:layout_constraintDimensionRatio="W,2:3"指的是 宽:高=2:3

**5、Barrier，设置栅栏，非常有用，某个view需要对齐另外一组view时用到：**

        <androidx.constraintlayout.widget.Barrier
            android:id="@+id/barrier"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            app:barrierDirection="top"
            app:constraint_referenced_ids="TextView1,TextView2,TextView2" />
