# 工程各个工具类说明

**1、BaseActivity 类** ：基础Activity类，把最外层的View 给封装好了，用的时候调用setContentLayout塞进去你的layout 即可。
     好处：便于统一管理页面，比如可以在页面内部显示加载框，可以添加右侧菜单，添加空View，无网页面等等。
     其中里面包含了Post() 网络请求的方法，建议使用这个请求网络，可以防止内存泄漏。
     BaseFragment里面的方法和和BaseActivity大体类似。所有的Fragment都要继承自BaseFragment

**2、debug切换**：我是在SD卡下面放置一个特殊文件，判断是否有这个文件来切换debug 环境。好处：可以随时把一个正式包设置为测试包，并且提交代码时再也不用去配置环境了。

**3、内建广播**：BaseActivity 里面我基于LocalManagerBroadcast 封装了一个广播，可以很方便的发广播接收广播，不需要注册，因为我自动帮你注册了。使用很方便，性能很高且资源消耗也很低。
     建议以后所有的异步消息传递都用这个，解耦性强。甚至startActivityForResult 都可以弃用，都统一使用内建广播传递消息。

**4、MyApp 类**：程序的入口，这里我把Application的Context 给持有了，获取的方法叫做getApp() 。切记，工程里凡是要用到Context的地方，MyApp.getApp() 方法，以免造成内存泄漏。
     还有，这里面有一句话叫做AppHelper.isAppMainProcess() ，是判断的主UI 进程的。
     因为Application的onCreate()有多少个进程就会执行多少次，而有些代码我们只需要在UI 进程执行，所以这里可以用上述方法判断。

**5、BaseListViewAdapter 类 && BaseRecyclerViewAdapter 类**：对BaseAdapter简单的封装了下，统一了增加数据，删除数据的方法。不用大家每个adapter都要写增删数据的方法，使用也很简单。
     为了以后的维护方便，建议大家都使用这个作为baseAdapter 。

**6、HttpUtils 类**：Http请求类，是基于OKHttp3封装的，让使用起来变的很简单，一眼就知道怎么用。

**7、SmartImageLoader 类**：图片加载类。使用的Glide ，并做了简单的封装，轻松实现圆角，封装的目的是以后可以方便的换其他的图片加载工具。

**8、PullToRefreshView 类**：实现了上拉加载更多，下拉刷新。封装自https://github.com/scwang90/SmartRefreshLayout ，它可以很随意的定制自己的header 和footer ，并有很多属性可以设置，极为好用。

**9、ReplaceViewHelper 类**：一个封装的工具类，可以很方便的替换任意的View 为另一个View 。

**10、LogUtils 类**：打Log日志的工具，debug 开启时才打Log .

**11、BaseUtils 类**：统一的工具类，里面包含了很多常用的方法并写了注释，大家可以看看，基本常用的方法都包含了。

**12、特别说明**：BaseUtils 里面有个方法叫做 getHandler() ，这是一个全局的Handler ，当然，我获取的是MainLooper 。以后大家执行post 或者postDelay操作时就可以直接用这个全局Handler 了。
    （特别注意，如果使用postDelay方法时，一定要记得onDestroy时调用removeCallback 方法，要不很容易内存泄漏）

**13、MyDialog 类**：封装了dialog ，系统的dialog样式太难看，并且根据手机的不同显示的样式也不同，所以我自己封装了一个。使用很简单，看一眼就会。

**14、ToastUtils 类**：统一弹Toast 的类，系统的Toast 样式太丑，这个是我自己定制的，可以随意定制，并且解决了系统toast 一直弹出的问题。

**15、ZoomImageView 支持缩放图的ImageView类**：https://github.com/chrisbanes/PhotoView

**16、BaseWebViewActivity 类**：基于腾讯X5浏览器内核封装的统一 WebViewActivity ，性能卓越。具体介绍可以去看X5内核。

**17、EasyCache 类**：一个基于文件的存储工具，可以很方便的存储很长的字符串，且效率很高。如果大家想做缓存，建议用这个。

**18、PopupWindowUtils 类**：我封装的可以在任意View 下方弹出popupWindows 的工具。使用很方便。

**19、SDCardUtils 类**：我封装的获取SD 卡存储路径的工具类，统一操作存储路径，不能随便在SD卡建立文件夹而影响用户体验。

**20、Constants 类**：公共常量类，存放了屏幕分辨率，全局gson 之类的，可以根据需要继续添加。

**21、MyUriUtils 类**：用schema 方式开发时的统一管理类，如果不用schema开发，就可以不用管这个。

**22、UserManager 类**：用户信息管理类，如果牵涉到用户登录之类的，要用这个类来管理，方便别人使用。

**23、FastBlurUtil 类**：我改良过的高斯模糊（毛玻璃）工具，效率非常高，使用很灵活。

      github上的两种实现：
      1、https://github.com/CameraKit/blurkit-android
      2、https://github.com/wasabeef/Blurry

**24、OnFinishListener 类** ：我自己定义的一个接口，可以作为一个万能回调使用。别的地方任何如果只是临时性的回调，可以用它来做，不需要再写个回调接口了。

**25、BitmapUtils 类**：压缩图片 以及得到图片宽高的类，里面有个方法叫createScaledBitmap() ，可以按比例创建缩放的图片，性能卓越。具体使用看注释。

**26、MyTabLayout 类**：根据谷歌官方的TabLayout自己封装的ViewPager标题指示器，实现了自定义View。

**27、NoScrollGridView 类**：不可滚动的GridView ，适用于放在ListView ，RecyclerView中。

**28、NoScrollViewPager 类**：不可滚动的ViewPager，具体使用场景自己发挥。

**29、com.youth.banner.Banner 类**：github 地址：//https://github.com/youth5201314/banner 一个很优秀的实现自动滚动banner的库。
      另外，里面有WeakHandler这个防止内存泄露的Handler类使用。还有各种的ViewPager Transformer可以使用。 。

**30、AutoSizeImageView 类**：按照Bitmap的宽高比，保持ImageView宽高比。需要在设置图片源之前调用setWidth(),setHeight()方法设置控件宽度，高度

**31、SelectPhotosActivity 类**：一个通用的相册图片选择类，支持单选和多选，用法看代码。

**32、EnglishCharFilter 类**：限制中文字符算作两个字，英文字符算作一个字的工具类。用法：

      editText.setFilters(new InputFilter[]{new EnglishCharFilter(MAX_COUNT)});

**33、SimpleAnimatorListener 类**：一个简单的动画监听类，目的是减少代码量。只监听了动画结束，因为动画结束是最常用的

**34、ZoomImageView 类**：一个支持手指缩放的ImageView ，支持在ViewPager中使用

**35、CustomViewPagerIndicator 类**：自定义的ViewPager指示器

**36、LongImageView 类**：自定义的用于显示长图的控件。基于WebView改造而来，性能卓越。

**37、CenterDrawable 类**：自定义的可以居中显示一个小图片的类。比如可以用于设置一个图片的未显示图片之前的默认图。
      用法：imageView.setImageDrawable(new CenterDrawable(R.drawable.image_loadding_icon))

**38、PolygonImageView 类**：github 地址：https://github.com/AlbertGrobas/PolygonImageView
      一个实现多边形的ImageView 类。四边形，五边形，六边形。另外还有星星，撕纸形状等，还可以自定义形状。

**39、RoundedImageView 类**：一个可以实现圆形图片、圆角图片的类。github 地址：https://github.com/vinc3m1/RoundedImageView

      app:riv_corner_radius="30dp" 圆角的角度
      app:riv_border_width="2dp" 图片边框的宽度
      app:riv_border_color="#333333" 图片边框的颜色
      app:riv_mutate_background="false" 是否需要显示控件的背景色，默认是显示
      app:riv_oval="true" 是否展示为圆形，如果true ，那么riv_corner_radius 不生效

**40、CornerLinearLayout && CornerRelativeLayout 类**：自定义的可以显示圆角的View 。在布局中设置圆角用android:tag="20"

**41、UpdateDialog 类**：一套完善的检查更新的对话框，稍作改动就可以实现复杂的检查更新逻辑

**42、AutoScrollRecyclerView 类**：无限循环的自动滚动的RecyclerView类，可以实现跑马灯效果，或者各种自动滚动效果。配合TestAutoScrollAdapter使用

**43、RippleView 类**：涟漪效果，类似雷达扫描

**44、ViewPagerLayoutManager 类**：RecyclerView的LayoutManager，仿抖音首页效果-横向和竖向滑动的viewPager
      ViewPagerLayoutManager.OnViewPagerListener 是滑动监听器

**43、SubsamplingScaleImageView 类**：一个可以显示长图的控件，和36不同的是，此类是自定义View实现的，推荐使用这个。
      github地址：https://github.com/davemorrissey/subsampling-scale-image-cheerly.mybaseproject.view

**44、TestFlexBoxActivity 类**：一个用谷歌FlexBox实现流式布局的demo类。谷歌官方出品的FlexBox，实现了流式布局。
      其中的FlexboxLayoutManager是为RecyclerView定制，可以实现流式布局。
      github地址：https://github.com/google/flexbox-layout

**45、com.github.zyyoona7:EasyPopup:1.1.1** ：一款强大，美观，优雅的通用弹窗XPopup，支持随意位置显示。
      github：https://github.com/zyyoona7/EasyPopup
   
**46、支持断点续传的下载**：1、https://github.com/AriaLyy/Aria  2、https://github.com/ixuea/AndroidDownloader

**47、封装的易用数据库dbflow**：https://github.com/agrosner/DBFlow 、 https://joyrun.github.io/2016/08/02/dbflow/

**48、  
48.1：沉浸式状态栏使用**：https://github.com/Zackratos/UltimateBar  
**48.2：设置状态栏颜色**：https://github.com/msdx/status-bar-compat  
**48.3：判断刘海屏的工具类**：NotchUtils.class
      
      // 如果使用48.2，那么在Android6.0以下会存在页面被状态栏遮挡了的bug，需要加上这段代码：
      // 注：viewLayout为当前Activity的根布局
          viewLayout.post(new Runnable() {
              @Override
              public void run() {
                  int[] location = new int[2];
                  viewLayout.getLocationOnScreen(location);
                  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                      int barHeight = BaseUtils.getStatusBarHeight();
                      if (location[1] < barHeight) {
                          RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewLayout.getLayoutParams();
                          params.topMargin = barHeight;
                          viewLayout.setLayoutParams(params);
                      }
                  }
              }
          });

**49、检测是否为模拟器的工具类**：EmulatorUtil.java

**50、图片裁剪器**：https://github.com/Yalantis/uCrop

**51、一款轻量级的socket库（模拟器Launcher在使用）**：https://github.com/xuuhaoo/OkSocket

**52、二维码扫描**：https://github.com/devilsen/CZXing(底层库用的是：https://github.com/nu-book/zxing-cpp)

**53、jackpal的Android-Terminal源码**：https://github.com/jackpal/Android-Terminal-Emulator/

**54、BottomDialogFragment**：类似抖音评论列表--滑动关闭的dialog(使用的是BottomSheetDialogFragment或者BottomSheetDialog)

**55、TestSnapNestViewPagerActivity**：使用Android自带的UI实现的带头部的嵌套滚动的ViewPager

**56、CommonFragmentViewPagerAdapter**：通用的ViewPager的FragmentAdapter

**57、TestGridViewWithHeaderActivity**：利用NestedScrollView实现RecyclerView带上header

**58、TestUserInfoViewModel**：LiveData和ViewModel的使用示例

**59、androidx.lifecycle.LifecycleObserver**：可以让随便一个类具有Activity的生命周期

**60、OnSingleClickListener**：防止重复多次点击的类

**60、js和android WebView通信**：https://github.com/lzyzsd/JsBridge

**61、集成各大push推送平台的库**：https://github.com/xuexiangjys/XPush/wiki

**62、手机和电脑屏幕共享的库，里面的TouchUtils可以实现转换触摸手势**：https://github.com/android-notes/androidScreenShare

**63、滑动表格库：ScrollablePanel**：https://github.com/Kelin-Hong/ScrollablePanel

**64、开源的视频播放器**：https://github.com/CarGuo/GSYVideoPlayer

**65、腾讯的多渠道打包**：https://github.com/Tencent/VasDolly

**66、直接获取TextView的LineCount的工具类，需要传入width**：TextViewLinesUtils.getTextViewLines(TextView textView, int textViewWidth);

**67、ConsecutiveScrollerLayout是Android下支持多个滑动布局**：(RecyclerView、WebView、ScrollView等)和普通控件(TextView、ImageView、LinearLayou、自定义View等)持续连贯滑动的容器,它使所有的子View像一个整体一样连续顺畅滑动。并且支持布局吸顶功能：https://github.com/donkingliang/ConsecutiveScroller

**68、一款时间选择器**：https://github.com/loperSeven/DateTimePicker

**69、GroupedRecyclerViewAdapter可以很方便的实现RecyclerView的分组显示，并且每个组都可以包含组头、组尾和子项；可以方便实现多种Type类型的列表，可以实现如QQ联系人的列表一样的列表展开收起功能，还可以实现头部悬浮吸顶功能等**：https://github.com/donkingliang/GroupedRecyclerViewAdapter 具体见DEMO:TestConsecutiveNestScrollActivity

**70、弹性动画实现**：

      70.1、谷歌的弹性动画：implementation 'androidx.dynamicanimation:dynamicanimation:1.0.0
            示例：http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0330/7757.html
      70.2、facebook的rebound：https://github.com/facebookarchive/rebound

**71、输入法键盘切换平滑过渡**：https://github.com/YummyLau/PanelSwitchHelper

**72、FlowLayout.java**：流式布局，来自腾讯团队的QMUIFloatLayout。 https://qmuiteam.com/android/documents/

**73、VerticalTextView.java**：来自腾讯团队的竖向排版的TextView

**74、实现高斯模糊的库**：https://github.com/woshidasusu/base-module/tree/master/blur

    引入：implementation 'com.dasu.image:blur:0.0.6'
    ------------------------------------------------
    用法：
    //1、使用默认配置，最短调用链
    Bitmap bitmap = DBlur.source(MainActivity.this).radius(5).sampling(8).build().doBlurSync();
    
    //2、同步模糊，将imageView控制的视图进行模糊，完成后自动显示到 imageView1 控件上，以淡入动画方式
    DBlur.source(imageView).intoTarget(imageView1).animAlpha().radius(5).sampling(8).build().doBlurSync();
    
    //3、异步模糊，将drawable资源文件中的图片以 NATIVE 方式进行模糊，注册回调，完成时手动显示到 imageView1 控件上
    DBlur.source(this, R.drawable.background).mode(BlurConfig.MODE_NATIVE).radius(5).sampling(8).build()
          .doBlur(new OnBlurListener() {
                @Override
                public void onBlurSuccess(Bitmap bitmap) {
                    imageView1.setImageBitmap(bitmap);
                }
    
                @Override
                public void onBlurFailed() {
                    //do something
                }});