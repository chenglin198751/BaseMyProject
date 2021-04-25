# 常用代码记录：

查看签名文件的MD5、SHA1、SHA-256：右侧Gradle：app--Tasks--android--signingReport

1、
1.1、注解表示枚举int型：@IntDef
注解表示枚举String型：@StringDef
代码示例如下：

    public static final int LANDSCAPE = 0;
    public static final int PORTRAIT = 1;

    @IntDef({LANDSCAPE, PORTRAIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScreenOrientation {
    }
    
    public static void setScreenOrientation(@ScreenOrientation final int orientation) {}

1.2、注解表示int型的范围：@IntRange(from = 0, to = 1)

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
    
5、监听Application的生命周期，比如App在前台后台。在Application的onCreate()中注册：

    ProcessLifecycleOwner.get().getLifecycle().addObserver(new LifecycleObserver());
    
6、View自带方法animate()实现动画的连续播放，先播放A动画，再播放B动画：

        mImageView.animate().scaleX(2f).setDuration(3000).withStartAction(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("tag_3","withStartAction");
                    }
                });
            }
        }).withEndAction(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.v("tag_3","withEndAction");
                        mImageView.animate().alpha(0f).setDuration(3000);
                    }
                });
            }
        }).start();
        
7、属性动画实现依次播放，连续播放，延迟播放：

        ObjectAnimator objectAnimator1 = ObjectAnimator.ofFloat(mImageView,"translationX", 150);
        ObjectAnimator objectAnimator2 = ObjectAnimator.ofFloat(mImageView,"alpha", 0.5f);
        ObjectAnimator objectAnimator3 = ObjectAnimator.ofFloat(mImageView,"y", 100);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(3000);
        set.playSequentially(objectAnimator1,objectAnimator2,objectAnimator3);//animators依次执行
        set.playTogether(objectAnimator1,objectAnimator2,objectAnimator3); //animators同时执行
        set.setStartDelay(500); //在start()后delay
        set.start();
        
        // 或者（这种不太好用）：
        ObjectAnimator objectAnimator01 = ObjectAnimator.ofArgb(mImageView, "BackgroundColor",
                getResources().getColor(R.color.colorPrimary),
                getResources().getColor(R.color.purple_button),
                getResources().getColor(R.color.holo_blue_light));
        ObjectAnimator objectAnimator02 = ObjectAnimator.ofFloat(mImageView, "TranslationY", 0, 300);
        ObjectAnimator objectAnimator03 = ObjectAnimator.ofFloat(mImageView, "TranslationX", 0, 400);
        ObjectAnimator objectAnimator04 = ObjectAnimator.ofFloat(mImageView, "ScaleY", 1, 0.5f);
        ObjectAnimator objectAnimator05 = ObjectAnimator.ofFloat(mImageView, "ScaleX", 1, 0.5f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(objectAnimator01).with(objectAnimator02).with(objectAnimator03).before(objectAnimator04).before(objectAnimator05);
        animatorSet.setDuration(2000);
        animatorSet.start();

8、RecyclerView监听childView被添加和剥离：
        recyclerView.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(View view) {
            }

            @Override
            public void onChildViewDetachedFromWindow(View view) {
            }
        });

9、获取mView在当前parentView的可见区域，以及当前整个窗口的可见区域
        Rect rect = new Rect();
        mView.getLocalVisibleRect(rect);

        Rect rect = new Rect();
        mView.getGlobalVisibleRect(rect);

        int[] local = new int[2];
        mBtnAdd.getLocationInWindow(local);

10、ConstraintLayout使用场景
        https://mp.weixin.qq.com/s/Z_TnoyMRYZEQXvlqiKX8Uw

11、优化recyclerView嵌套recyclerView时，view没复用导致的卡顿
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        mRecyclerView.setRecycledViewPool(viewPool);

12、ViewPager执行假滑动：
        if (!mViewPager.isFakeDragging()) {
            mViewPager.beginFakeDrag();
        }
        if (mViewPager.isFakeDragging()) {
            mViewPager.fakeDragBy(-1f);
        }
        if (mViewPager.isFakeDragging()){
            mViewPager.endFakeDrag();
        }