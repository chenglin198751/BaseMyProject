# 工程各个工具类说明

* [AndroidStudio技巧](doc_notes/AndroidStudio技巧.md)
* [ConstraintLayout学习.md](doc_notes/ConstraintLayout学习.md)
* [gradle_note.md](doc_notes/gradle_note.md)
* [Kotlin.md](doc_notes/Kotlin.md)
* [Linux学习笔记.md](doc_notes/Linux学习笔记.md)
* [常用代码记录1.md](doc_notes/常用代码记录1.md)
* [常用代码记录2.md](doc_notes/常用代码记录2.md)
* [开发SDK注意事项.md](doc_notes/开发SDK注意事项.md)

**1、BaseActivity 类：** 

    基础Activity类，把最外层的View 给封装好了，用的时候调用setContentLayout塞进去你的layout 即可。
    好处：便于统一管理页面，比如可以在页面内部显示加载框，可以添加右侧菜单，添加空View，无网页面等等。
    其中里面包含了Post() 网络请求的方法，建议使用这个请求网络，可以防止内存泄漏。
    BaseFragment里面的方法和和BaseActivity大体类似。所有的Fragment都要继承自BaseFragment

**2、debug切换：**
    
    EnvToggle.java 和 在拨号键盘输入 *#*#2022360#*#* 可以打开debug模式。具体见：DialPhoneBroadcastReceiver.java类

**3、内建广播：**

    BaseActivity 里面基于LocalManagerBroadcast 封装了一个广播，可以很方便的发广播接收广播，不需要注册，因为已经自动注册了。
    使用很方便，性能很高且资源消耗也很低。建议以后所有的异步消息传递都用这个，解耦性强。。

**4、MainApp 类：**

    程序的入口，这里我把Application的Context 给持有了，获取的方法叫做getApp() 。
    切记，工程里凡是要用到Context的地方，MainApp.getApp() 方法，以免造成内存泄漏。
    还有，这里面有一句话叫做AppHelper.isAppMainProcess() ，是判断的主UI 进程的。
    因为Application的onCreate()有多少个进程就会执行多少次，而有些代码我们只需要在UI 进程执行，所以这里可以用上述方法判断。

**5、BaseListViewAdapter 类 && BaseRecyclerViewAdapter 类：**

    对BaseAdapter简单的封装了下，统一了增加数据，删除数据的方法。不用大家每个adapter都要写增删数据的方法，使用也很简单。
    为了以后的维护方便，建议大家都使用这个作为baseAdapter 。

**6、HttpUtils 类：**

    Http请求类，是基于OKHttp3封装的，包含文件下载，让使用起来变的很简单，一眼就知道怎么用。

**7、SmartImageLoader 类：**

    图片加载类。使用的Glide ，并做了简单的封装，轻松实现圆角，封装的目的是以后可以方便的换其他的图片加载工具。

**8、PullToRefreshView 类：**
    
    实现了上拉加载更多，下拉刷新。封装自https://github.com/scwang90/SmartRefreshLayout ，它可以很随意的定制自己的header 和footer ，并有很多属性可以设置，极为好用。

**9、ReplaceViewHelper 类：**

    一个封装的工具类，可以很方便的替换任意的View 为另一个View 。

**10、AppLogUtils 类：**
    
    打Log日志的工具，debug 开启时才打Log 

**11、AppBaseUtils 类：**

    统一的工具类，里面包含了很多常用的方法并写了注释，基本常用的方法都包含了。
    特别说明：
    AppBaseUtils 里面有个方法叫做 getUiHandler() ，这是一个全局的Handler ，当然，获取的是MainLooper 。以后大家执行post 或者postDelay操作时就可以直接用这个全局Handler 了。
    （特别注意，如果使用postDelay方法时，一定要记得onDestroy时调用removeCallback 方法，要不很容易内存泄漏）

**13、CommonDialog 类：**

    封装了dialog ，系统的dialog样式太难看，并且根据手机的不同显示的样式也不同，所以自己封装了一个。使用很简单，看一眼就会。

**14、ToastUtils 类：**

    统一弹Toast 的类，系统的Toast 样式太丑，这个是自己定制的，可以随意定制，并且解决了系统toast 一直弹出的问题。

**15、ZoomImageView 支持缩放图的ImageView类：**

    https://github.com/chrisbanes/PhotoView

**16、BaseWebViewActivity.java,BaseWebViewFragment.java类：**

    基于腾讯X5浏览器内核封装的统一 WebViewActivity ，性能卓越。具体介绍可以去看X5内核。

**17、EasyCache 类：**

    一个基于文件的存储工具，可以很方便的存储很长的字符串，且效率很高。如果想做缓存，建议用这个。

**18、PopupWindowUtils 类：**

    封装的可以在任意View 下方弹出popupWindows 的工具。使用很方便。

**19、FileUtils 类：**

    获取存储路径的工具类，由于获取的是内部的存储路径，所以不需要存储权限。

**20、AppConstants 类：**

    公共常量类，存放了屏幕分辨率，全局gson 之类的，可以根据需要继续添加。

**21、MyUriUtils 类：**

    用schema 方式开发时的统一管理类，如果不用schema开发，就可以不用管这个。

**22、UserManager 类：**

    用户信息管理类，如果牵涉到用户登录之类的，要用这个类来管理，方便别人使用。

**23、FastBlurUtil,FastBlurUtil2 类：**

    我改良过的高斯模糊（毛玻璃）工具，效率非常高，使用很灵活。建议使用FastBlurUtil。
    github上的两种实现：
    1、https://github.com/CameraKit/blurkit-android
    2、https://github.com/wasabeef/Blurry

**24、OnFinishedListener 类：** 

    自己定义的一个公共回调接口，可以作为一个万能回调使用。别的地方任何如果只是临时性的回调，可以用它来做，不需要再写个回调接口了。

**25、BitmapUtils 类：**

    压缩图片 以及得到图片宽高的类，里面有个方法叫createScaledBitmap() ，可以按比例创建缩放的图片，性能卓越。具体使用看注释。

**26、MyTabLayout 类：**

    根据谷歌官方的TabLayout自己封装的ViewPager标题指示器，实现了自定义View。

**27、NoScrollGridView 类：**

    不可滚动的GridView ，适用于放在ListView ，RecyclerView中。

**28、NoScrollViewPager 类：**

    不可滚动的ViewPager，具体使用场景自己发挥。

**29、com.youth.banner.Banner 类：**

    github 地址：//https://github.com/youth5201314/banner 一个很优秀的实现自动滚动banner的库。
    另外，里面有WeakHandler这个防止内存泄露的Handler类使用。还有各种的ViewPager Transformer可以使用。 

**30、AutoSizeImageView 类：**

    按照Bitmap的宽高比，保持ImageView宽高比。需要在设置图片源之前调用setWidth(),setHeight()方法设置控件宽度，高度

**32、EnglishCharFilter 类：**限制中文字符算作两个字，英文字符算作一个字的工具类。用法：

      editText.setFilters(new InputFilter[]{new EnglishCharFilter(MAX_COUNT)});

**33、SimpleAnimatorListener 类：**

    一个简单的动画监听类，目的是减少代码量。只监听了动画结束，因为动画结束是最常用的

**34、ZoomImageView 类：**

    一个支持手指缩放的ImageView ，支持在ViewPager中使用

**35、CustomViewPagerIndicator 类：**

    自定义的ViewPager指示器

**36、LongImageView 类：**

    自定义的用于显示长图的控件。基于WebView改造而来，性能卓越。

**37、CenterDrawable 类：**

    自定义的可以居中显示一个小图片的类。比如可以用于设置一个图片的未显示图片之前的默认图。
    用法：imageView.setImageDrawable(new CenterDrawable(R.drawable.image_loadding_icon))

**38、PolygonImageView 类：**

    github 地址：https://github.com/AlbertGrobas/PolygonImageView
    一个实现多边形的ImageView 类。四边形，五边形，六边形。另外还有星星，撕纸形状等，还可以自定义形状。

**39、RoundedImageView 类：**

    一个可以实现圆形图片、圆角图片的类。github 地址：https://github.com/vinc3m1/RoundedImageView
      app:riv_corner_radius="30dp" 圆角的角度
      app:riv_border_width="2dp" 图片边框的宽度
      app:riv_border_color="#333333" 图片边框的颜色
      app:riv_mutate_background="false" 是否需要显示控件的背景色，默认是显示
      app:riv_oval="true" 是否展示为圆形，如果true ，那么riv_corner_radius 不生效

**40、CornerLinearLayout && CornerRelativeLayout 类：**

    自定义的可以显示圆角的View 。在布局中设置圆角用android:tag="20"

**41、UpdateDialog 类：**

    一套完善的检查更新的对话框，稍作改动就可以实现复杂的检查更新逻辑

**42、AutoScrollRecyclerView 类：**

    无限循环的自动滚动的RecyclerView类，可以实现跑马灯效果，或者各种自动滚动效果。配合TestAutoScrollAdapter使用

**43、RippleView 类：**

    涟漪效果，类似雷达扫描

**44、ViewPagerLayoutManager 类：**

    RecyclerView的LayoutManager，仿抖音首页效果-横向和竖向滑动的viewPager
    ViewPagerLayoutManager.OnViewPagerListener 是滑动监听器

**43、SubsamplingScaleImageView 类：**

    一个可以显示长图的控件，和36不同的是，此类是自定义View实现的，推荐使用这个。
    github地址：https://github.com/davemorrissey/subsampling-scale-image-com.wcl.test.view

**44、TestFlexBoxActivity 类：**
    
    一个用谷歌FlexBox实现流式布局的demo类。谷歌官方出品的FlexBox，实现了流式布局。
    其中的FlexboxLayoutManager是为RecyclerView定制，可以实现流式布局。
    github地址：https://github.com/google/flexbox-layout

**45、com.github.zyyoona7:EasyPopup:1.1.1：** 

    一款强大，美观，优雅的通用弹窗XPopup，支持随意位置显示。
    github：https://github.com/zyyoona7/EasyPopup
   
**46、支持断点续传的下载：**

    1、https://github.com/AriaLyy/Aria 
    2、https://github.com/ixuea/AndroidDownloader
    3、HttpURLConnection实现的断点续传：https://github.com/yaowen369/DownloadHelper
    4、https://github.com/lingochamp/okdownload

**47、封装的易用数据库dbflow：**

    https://github.com/agrosner/DBFlow ： https://joyrun.github.io/2016/08/02/dbflow/

**49、检测是否为模拟器的工具类：**EmulatorUtil.java

**50、图片裁剪器：**https://github.com/Yalantis/uCrop

**51、一款轻量级的socket库（模拟器Launcher在使用）：**https://github.com/xuuhaoo/OkSocket

**52、二维码扫描：**https://github.com/devilsen/CZXing(底层库用的是：https://github.com/nu-book/zxing-cpp)

**53、jackpal的Android-Terminal源码：**https://github.com/jackpal/Android-Terminal-Emulator/

**54、BottomDialogFragment：**类似抖音评论列表--滑动关闭的dialog(使用的是BottomSheetDialogFragment或者BottomSheetDialog)

**55、TestSnapNestViewPagerActivity：**使用Android自带的UI实现的带头部的嵌套滚动的ViewPager

**56、CommonFragmentViewPagerAdapter：**通用的ViewPager的FragmentAdapter

**57、TestGridViewWithHeaderActivity：**利用NestedScrollView实现RecyclerView带上header

**58、TestUserInfoViewModel：**LiveData和ViewModel的使用示例

**59、androidx.lifecycle.LifecycleObserver：**可以让随便一个类具有Activity的生命周期

**60、OnSingleClickListener：**防止重复多次点击的类

**60、js和android WebView通信：**https://github.com/lzyzsd/JsBridge

**61、集成各大push推送平台的库：**https://github.com/xuexiangjys/XPush/wiki

**62、手机和电脑屏幕共享的库，里面的TouchUtils可以实现转换触摸手势：**https://github.com/android-notes/androidScreenShare

**63、滑动表格库：ScrollablePanel：**https://github.com/Kelin-Hong/ScrollablePanel

**64、开源的视频播放器：**https://github.com/CarGuo/GSYVideoPlayer

**65、腾讯的多渠道打包：**https://github.com/Tencent/VasDolly

**66、直接获取TextView的LineCount的工具类，需要传入width：**TextViewLinesUtils.getTextViewLines(TextView textView, int textViewWidth);

**67、ConsecutiveScrollerLayout是Android下支持多个滑动布局：**(RecyclerView、WebView、ScrollView等)和普通控件(TextView、ImageView、LinearLayou、自定义View等)持续连贯滑动的容器,它使所有的子View像一个整体一样连续顺畅滑动。并且支持布局吸顶功能：https://github.com/donkingliang/ConsecutiveScroller

**68、一款时间选择器：**https://github.com/loperSeven/DateTimePicker

**69、GroupedRecyclerViewAdapter可以很方便的实现RecyclerView的分组显示，并且每个组都可以包含组头、组尾和子项；可以方便实现多种Type类型的列表，可以实现如QQ联系人的列表一样的列表展开收起功能，还可以实现头部悬浮吸顶功能等：**https://github.com/donkingliang/GroupedRecyclerViewAdapter 具体见DEMO:TestConsecutiveNestScrollActivity

**70、弹性动画实现：**

      70.1、谷歌的弹性动画：implementation 'androidx.dynamicanimation:dynamicanimation:1.0.0
            示例：http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2017/0330/7757.html
      70.2、facebook的rebound：https://github.com/facebookarchive/rebound

**71、输入法键盘切换平滑过渡：**https://github.com/YummyLau/PanelSwitchHelper

**72、FlowLayout.java：**流式布局，来自腾讯团队的QMUIFloatLayout。 https://qmuiteam.com/android/documents/

**73、VerticalTextView.java：**来自腾讯团队的竖向排版的TextView

**74、实现高斯模糊的库：**https://github.com/woshidasusu/base-module/tree/master/blur

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

**75、各种ViewPager标题指示器效果：**https://github.com/hackware1993/MagicIndicator    

**76、支持列表中播放的视频播放器饺子，支持替换为谷歌EXO内核：**https://github.com/Jzvd/JZVideo

**77、B站开源播放器ijkplayer：**https://github.com/bilibili/ijkplayer

**78、可以下拉头部放大的RecyclerView，参考其用法可以实现别的动效：**PullToZoomRecyclerView.java

**79、PullScrollView：**重写overScrollBy()方法可以实现下拉交互特效

**80、App实现黑白模式：**在BaseActivity的onCreate()种实现：

    Paint paint = new Paint();
    ColorMatrix cm = new ColorMatrix();
    cm.setSaturation(0);
    paint.setColorFilter(new ColorMatrixColorFilter(cm));
    getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE,paint);

**81、MMKV键值对存储，可替代SP使用：**https://github.com/Tencent/MMKV/blob/master/README_CN.md

**82、安卓开源的图表库：**
    https://github.com/danielgindi/Charts
    https://github.com/PhilJay/MPAndroidChart

**83、监听键盘输入法弹出隐藏：**https://github.com/yshrsmz/KeyboardVisibilityEvent

**84、gson解析容错：**https://github.com/getActivity/GsonFactory

**85、facebook开源的发光渐变文字效果：**http://facebook.github.io/shimmer-android/ ; ShimmerTextView ：简易版的发光渐变文字效果

**86、ShapeableImageView：**谷歌官方material系列，实现圆形，圆角，等各种形状的ImageView

**87、APK反编译：**反编译工具：https://github.com/skylot/jadx/releases/tag/v1.2.0  源码：https://github.com/skylot/jadx

**88、计算签名的工具类 SignUtils.java：**

    final String appSecret = "c97d25b2518745b4a02fa43934e951b5";
    Map<String, String> orderParam = new HashMap<>();
    orderParam.put("order_id","163549389998541021");
    orderParam.put("product_name","vipCool");
    String sign = SignUtils.getSign(orderParam, appSecret);

**89、用HttpURLConnection封装的一套网络访问工具：**https://github.com/guozhengXia/UrlHttpUtils

**90、横屏模式下，EditText唤起键盘时，键盘全屏，导致无法看到输入页面。加如下属性禁止键盘全屏：：**

    android:imeOptions="flagNoExtractUi"
    mUserNameText.setImeOptions(|EditorInfo.IME_FLAG_NO_EXTRACT_UI);

**91、通过标签直接生成shape，无需再写shape.xml：**https://github.com/JavaNoober/BackgroundLibrary

**92、aar接入方式开发SDK可以使用自定义的ContentProvider初始化SDK：**com.wcl.test.CustomInitProvider，其onCreate()方法，晚于Application的attachBaseContext()，早于Application的onCreate()。

**93、glide自定义各种变换形状，灰度，黑白等：** implementation 'jp.wasabeef:glide-transformations:4.3.0'
    
    //比如，这个是图片从顶部开始展示
    Glide.with(getContext())
    .load(url)
    .transform(new CropTransformation(100,100, CropTransformation.CropType.TOP))
    .into(image);

**94、Gson的容错兼容库：**https://github.com/getActivity/GsonFactory

**95、WindowInsetsControllerCompat隐藏显示键盘：**

    windowInsetsControllerCompat.show(WindowInsetsCompat.Type.ime());
    windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.ime());

**96、WorkManager：**实现看TestWorkManager.java ; 参考文档：https://www.jianshu.com/p/284700112f37

    特点： 1、针对不需要及时完成的任务 2、保证任务一定会被执行（只要提交了任务，任务如果返回的是retry，那么就会一直重试，保证一定被执行，不管杀掉程序还是重启手机）

**97、解决Android P以上不让反射调用hide api的问题：**implementation "com.github.tiann:FreeReflection:3.1.0"

**98、滴滴开源的字节码替换工具：**https://github.com/didi/DroidAssist

**99、TextClock可以监听系统时间：**
        
        //1、Activity不可见时TextClock绘制停止，监听也会停止
        textClock.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence time, int start, int before, int count) {
                Log.v("tag_99","time = " + time);
            }
        });

        //2、还可以监听系统时间广播
        BroadcastReceiver mTimeUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
                    Log.v("tag_99","系统每1分钟发送一次广播");
                } 
            }
        };

**100、google官方推荐的单例写法：**

    public class SdkActivityLife{
        private SdkActivityLife() {
        }
        private static final class InstanceHolder {
            static final SdkActivityLife INSTANCE = new SdkActivityLife();
        }
        public static SdkActivityLife getInstance() {
            return InstanceHolder.INSTANCE;
        }
    }

**101、Collections类的功能：**

    1、列表元素随机：Collections.shuffle(list)
    2、合并两个list：Collections.addAll(list,list);
    3、对列表排序：Collections.sort(list,Comparator);
    4、查找某个元素的位置：Collections.binarySearch(list,"55555");
    5、把第0个元素和第2个元素交换位置：Collections.swap(list,0,2);
    6、把一个list复制到另一个list：Collections.copy(list,list);

**102、腾讯性能监测开源工具：**

    https://github.com/Tencent/matrix#matrix_cn

**103、android官方百分比布局：**implementation 'androidx.percentlayout:percentlayout:1.0.0'

    app:layout_heightPercent="30%"
    app:layout_marginTopPercent="10%"
    app:layout_widthPercent="100%"

**104、调用系统的选择照片的工具类：**

    PhotosPickUtil.java，不需要存储权限就可以选择照片，但是只能每次选择一张。

**105、完全仿微信的图片选择，并且提供了多种图片加载接口，选择图片后可以旋转，可以裁剪成矩形或圆形，可以配置各种其他的参数：**

    https://github.com/jeasonlzy/ImagePicker
