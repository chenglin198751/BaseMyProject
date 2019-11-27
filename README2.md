### 常用代码记录：

1、
注解表示枚举int型：@IntDef
注解表示枚举String型：@StringDef
代码示例如下：

    public static final int LANDSCAPE = 0;
    public static final int PORTRAIT = 1;

    @IntDef({LANDSCAPE, PORTRAIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenOrientation {
    }
    
    public static void setScreenOrientation(@ScreenOrientation final int orientation) {}

注解表示int型的范围：@IntRange(from = 0, to = 1)

2、WorkManager
参考博客：https://blog.csdn.net/fengyeNom1/article/details/90289700
处理工作流，可以取代service。即使应用被杀了，手机重启，一旦再次打开应用，满足条件了即刻执行。
可以设置约束条件，比如网络连接时执行，手机充电时执行等。还可以设置重复执行任务（间隔时间最短为15分钟）。
还可以设置任务执行的先后顺序。基础代码如下：

    // 不能是内部类，必须是正常的外部类或者static内部类。否则不执行
    public class MouseWorker extends Worker {
        public MouseWorker(@NonNull Context context,
                           @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }
    
        @NonNull
        @Override
        public Result doWork() {
            // 任务开始执行 ToDo 
            Log.v("tag_3", "isUiThread() = " + BaseUtils.isUiThread());
            return Result.success();
        }
    }
    
    // 发起任务
    OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MouseWorker.class)
            .setInitialDelay(2, TimeUnit.MILLISECONDS)
            .build();
    WorkManager.getInstance().enqueue(workRequest);
    
3、SystemClock.uptimeMillis()从开机到现在的毫秒数，不会因为用户修改了手机时间而受影响。

4、LifecycleObserver实现Activity生命周期监听：

    //4.1：实现LifecycleObserver接口来定义监听器：
    public class MyObserver implements LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        public void onCreate() {
            Log.v("tag_3","onCreate()");
        }
    
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        public void onDestroy() {
            Log.v("tag_3","onDestroy()");
        }
    }
    
    //4.2：在Activity或者Fragment中注册：
    getLifecycle().addObserver(new MyObserver());