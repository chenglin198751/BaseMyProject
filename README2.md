### 常用代码记录：

1、
注解表示枚举int型：@IntDef
注解表示枚举String型：@StringDef
代码示例如下：

    public static final int LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    public static final int PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    @IntDef({LANDSCAPE, PORTRAIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenOrientation {
    }
    
    public static void setScreenOrientation(@ScreenOrientation final int orientation) {}

注解表示int型的范围：@IntRange(from = 0, to = 1)




