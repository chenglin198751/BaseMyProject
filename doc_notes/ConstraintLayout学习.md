# ConstraintLayout学习：

**1、ConstraintLayout使用说明：** 

    https://mp.weixin.qq.com/s/Z_TnoyMRYZEQXvlqiKX8Uw
    官方不推荐在ConstraintLayout中使用match_parent，可以设置 0dp 配合约束代替match_parent

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
    app:layout_constrainedWidth="true"
    app:layout_constrainedHeight="true

**5、Barrier，设置栅栏，非常有用，某个view需要对齐另外一组view时用到：**

    <androidx.constraintlayout.widget.Barrier
        android:id="@+id/barrier"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        app:barrierDirection="top"
        app:constraint_referenced_ids="TextView1,TextView2,TextView2" />

**6、Group，把一些view设置为一组，一般用来隐藏显示一组view**

    <androidx.constraintlayout.widget.Group
        android:id="@+id/count_group"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="10dp"
        app:constraint_referenced_ids="TextView1,TextView2,TextView2" />

**7、等分布局的两种方式：**

    第1种：这种方式是把当前屏幕宽度设置为1，然后每个view占据百分比：
        app:layout_constraintHorizontal_chainStyle="packed"
        app:layout_constraintWidth_percent="0.3"

    第2种：这种方式是把当前屏幕宽度按照比重分配：
        app:layout_constraintHorizontal_chainStyle="packed"
        app:layout_constraintHorizontal_weight="1"

**8、放置在id为grid_layout的视图下方，并且占据grid_layout下方所有剩余空间：**

    <RelativeLayout
        android:id="@+id/relative_layout"
        android:layout_width="0dp"
        android:layout_height="0dp"
        android:background="@color/black"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintRight_toRightOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/grid_layout">
        
    </RelativeLayout>

**10、角度定位（textView2设置在textView1的右下角120°距离80dp的位置）：**

    layout_constraintCircle : 引用另一个小部件 id
    layout_constraintCircleRadius : 到另一个小部件中心的距离
    layout_constraintCircleAngle : 小部件应该在哪个角度（以度为单位，从 0 到 360）

    // xml代码示例：
    <TextView
        android:id="@+id/textView1"
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:background="@android:color/holo_red_light"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toBottomOf="@+id/grid_layout" />
    <TextView
        android:id="@+id/textView2"
        android:layout_width="20dp"
        android:layout_height="20dp"
        android:background="@android:color/holo_blue_dark"
        app:layout_constraintCircle="@+id/textView1"
        app:layout_constraintCircleAngle="120"
        app:layout_constraintCircleRadius="80dp"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

**11、比如textView2的锚点是textView1，那么设置以下属性可以解决锚点textView1被gone的边距问题：**

    layout_goneMarginStart
    layout_goneMarginEnd  
    layout_goneMarginLeft
    layout_goneMarginTop
    layout_goneMarginRight
    layout_goneMarginBottom

**12、居中设置：**

    // 垂直水平居中：
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent"

    // 垂直居中
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintTop_toTopOf="parent"

    // 水平居中
    app:layout_constraintLeft_toLeftOf="parent"
    app:layout_constraintRight_toRightOf="parent"

**13、Placeholder占位符。可以让@+id/textView1直接移动到@+id/placeholder的位置：**

    <androidx.constraintlayout.widget.Placeholder
        android:id="@+id/placeholder"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:layout_marginLeft="100dp"
        app:content="@+id/textView1"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
    <TextView
        android:id="@+id/textView1"
        android:layout_width="100dp"
        android:layout_height="100dp"
        android:background="@android:color/holo_blue_light"
        android:text="TextView1"
        app:layout_constraintLeft_toLeftOf="parent"
        app:layout_constraintTop_toTopOf="parent" />